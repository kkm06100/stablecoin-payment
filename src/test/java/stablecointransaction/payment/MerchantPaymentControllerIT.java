package stablecointransaction.payment;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.util.UUID;
import stablecointransaction.external.port.TokenAccountRegistrar;
import stablecointransaction.external.port.WalletProvisioner;
import stablecointransaction.merchant.dto.CreateMerchantRequest;
import stablecointransaction.merchant.outbox.MerchantOutboxProcessor;
import stablecointransaction.payment.dto.CreatePaymentRequest;
import stablecointransaction.support.TestDatabaseCleaner;
import stablecointransaction.userauth.dto.AuthTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.task.scheduling.enabled=false")
class MerchantPaymentControllerIT {
  @Autowired MockMvc mvc;
  @Autowired MerchantOutboxProcessor merchantOutboxProcessor;
  @Autowired ObjectMapper objectMapper;
  @Autowired JdbcTemplate jdbc;

  @MockitoBean WalletProvisioner walletProvisioner;
  @MockitoBean TokenAccountRegistrar tokenAccounts;

  @BeforeEach
  void setUp() {
    TestDatabaseCleaner.cleanApplicationState(jdbc);
    when(walletProvisioner.create(any()))
        .thenAnswer(invocation -> new WalletProvisioner.ProvisionedWallet(
            UUID.randomUUID(), invocation.getArgument(0), "solana", "user", null, "ACTIVE"));
    doNothing().when(tokenAccounts).register(any(), any());
  }

  @Test
  void createsPaymentAndQrThroughMerchantEndpoint() throws Exception {
    Session session = signup("merchant-it@example.com");
    UUID merchantId = createMerchant(session);

    mvc.perform(post("/v1/merchants/{merchantId}/payments", merchantId)
            .with(jwt().jwt(jwt -> jwt.subject(session.userId().toString())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(new CreatePaymentRequest(
                "order-it-1", "USDC-test", BigInteger.ONE, "integration payment"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.payment_id").isNotEmpty())
        .andExpect(jsonPath("$.merchant_id").value(merchantId.toString()))
        .andExpect(jsonPath("$.status").value(PaymentStatuses.CREATED))
        .andExpect(jsonPath("$.qr_payload").value(containsString("/v1/payment-qr/")))
        .andExpect(jsonPath("$.expires_at").isNotEmpty());
  }

  @Test
  void rejectsPaymentCreationByUserWithoutMerchantMembership() throws Exception {
    Session owner = signup("owner-it@example.com");
    Session anotherUser = signup("another-it@example.com");
    UUID merchantId = createMerchant(owner);

    mvc.perform(post("/v1/merchants/{merchantId}/payments", merchantId)
            .with(jwt().jwt(jwt -> jwt.subject(anotherUser.userId().toString())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(new CreatePaymentRequest(
                "order-it-2", "USDC-test", BigInteger.ONE, "unauthorized"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void rejectsSameOrderWithDifferentPaymentFields() throws Exception {
    Session session = signup("duplicate-it@example.com");
    UUID merchantId = createMerchant(session);
    CreatePaymentRequest first = new CreatePaymentRequest(
        "order-it-3", "USDC-test", BigInteger.ONE, "first");

    mvc.perform(post("/v1/merchants/{merchantId}/payments", merchantId)
            .with(jwt().jwt(jwt -> jwt.subject(session.userId().toString())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(first)))
        .andExpect(status().isOk());

    mvc.perform(post("/v1/merchants/{merchantId}/payments", merchantId)
            .with(jwt().jwt(jwt -> jwt.subject(session.userId().toString())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(new CreatePaymentRequest(
                "order-it-3", "USDC-test", BigInteger.TWO, "different"))))
        .andExpect(status().isConflict());
  }

  @Test
  void getsPaymentDetailAndMerchantPaymentList() throws Exception {
    Session session = signup("query-it@example.com");
    UUID merchantId = createMerchant(session);
    String response = mvc.perform(post("/v1/merchants/{merchantId}/payments", merchantId)
            .with(jwt().jwt(jwt -> jwt.subject(session.userId().toString())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(new CreatePaymentRequest(
                "order-it-query", "USDC-test", BigInteger.ONE, "query payment"))))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    UUID paymentId = UUID.fromString(objectMapper.readTree(response)
        .get("payment_id").asText());

    mvc.perform(get("/v1/merchants/{merchantId}/payments/{paymentId}", merchantId, paymentId)
            .with(jwt().jwt(jwt -> jwt.subject(session.userId().toString()))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.payment_id").value(paymentId.toString()))
        .andExpect(jsonPath("$.order_id").value("order-it-query"));

    mvc.perform(get("/v1/merchants/{merchantId}/payments", merchantId)
            .param("limit", "10")
            .with(jwt().jwt(jwt -> jwt.subject(session.userId().toString()))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.payments", hasSize(1)))
        .andExpect(jsonPath("$.payments[0].payment_id").value(paymentId.toString()));
  }

  @Test
  void rejectsInvalidPaymentRequestAtControllerBoundary() throws Exception {
    Session session = signup("validation-it@example.com");
    UUID merchantId = createMerchant(session);

    mvc.perform(post("/v1/merchants/{merchantId}/payments", merchantId)
            .with(jwt().jwt(jwt -> jwt.subject(session.userId().toString())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(new CreatePaymentRequest(
                "order-it-invalid", "USDC-test", BigInteger.ZERO, "invalid amount"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
  }

  @Test
  void requiresAuthenticationForPaymentEndpoints() throws Exception {
    mvc.perform(post("/v1/merchants/{merchantId}/payments", UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(new CreatePaymentRequest(
                "order-it-anonymous", "USDC-test", BigInteger.ONE, "anonymous"))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void requiresAuthenticationForCustomerPaymentHistory() throws Exception {
    mvc.perform(get("/v1/payments"))
        .andExpect(status().isUnauthorized());

    mvc.perform(get("/v1/payments/{paymentId}", UUID.randomUUID()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void requiresAuthenticationForCustomerWallet() throws Exception {
    mvc.perform(get("/v1/me/wallet"))
        .andExpect(status().isUnauthorized());
  }

  private Session signup(String email) throws Exception {
    String response = mvc.perform(post("/v1/user-auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(new SignupPayload(email, "password123", "IT user"))))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    AuthTokenResponse auth = objectMapper.readValue(response, AuthTokenResponse.class);
    return new Session(auth.user_id(), auth.access_token());
  }

  private UUID createMerchant(Session session) throws Exception {
    String response = mvc.perform(post("/v1/merchants")
            .with(jwt().jwt(jwt -> jwt.subject(session.userId().toString())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(new CreateMerchantRequest("IT Merchant", "it-" + UUID.randomUUID()
                .toString().replace("-", "").substring(0, 20)))))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    UUID merchantId = UUID.fromString(objectMapper.readTree(response).get("merchant_id").asText());
    merchantOutboxProcessor.processOne();
    return merchantId;
  }

  private String json(Object value) throws Exception {
    return objectMapper.writeValueAsString(value);
  }

  private record Session(UUID userId, String accessToken) {}
  private record SignupPayload(String email, String password, String display_name) {}
}
