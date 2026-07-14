package stablecointransaction.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.exception.InternalApplicationException;
import stablecointransaction.external.exception.StablecoinTransactionRemoteException;
import stablecointransaction.external.port.TokenAccountRegistrar;
import stablecointransaction.external.port.TransferGateway;
import stablecointransaction.external.port.WalletProvisioner;
import stablecointransaction.merchant.dto.CreateMerchantRequest;
import stablecointransaction.merchant.outbox.MerchantOutboxProcessor;
import stablecointransaction.payment.outbox.PaymentOutboxProcessor;
import stablecointransaction.payment.outbox.PaymentOutboxStatuses;
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
class PaymentOutboxIT {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired JdbcTemplate jdbc;
  @Autowired PaymentOutboxProcessor outboxProcessor;
  @Autowired MerchantOutboxProcessor merchantOutboxProcessor;

  @MockitoBean WalletProvisioner walletProvisioner;
  @MockitoBean TokenAccountRegistrar tokenAccounts;
  @MockitoBean TransferGateway transferGateway;

  @BeforeEach
  void setUp() {
    TestDatabaseCleaner.cleanApplicationState(jdbc);
    when(walletProvisioner.create(any()))
        .thenAnswer(invocation -> new WalletProvisioner.ProvisionedWallet(
            UUID.randomUUID(), invocation.getArgument(0), "solana", "user", null, "ACTIVE"));
    doNothing().when(tokenAccounts).register(any(), any());
  }

  @Test
  void confirmsPaymentThroughOutboxWorker() throws Exception {
    Session session = signup("outbox-it@example.com");
    UUID merchantId = createMerchant(session);
    JsonNode created = createPayment(session, merchantId);
    UUID paymentId = UUID.fromString(created.get("payment_id").asText());
    String rawToken = created.get("qr_payload").asText()
        .substring(created.get("qr_payload").asText().lastIndexOf('/') + 1);

    mvc.perform(post("/v1/payment-qr/{token}/confirm", rawToken)
            .with(jwt().jwt(jwt -> jwt.subject(session.userId().toString()))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.payment_id").value(paymentId.toString()))
        .andExpect(jsonPath("$.status").value(PaymentStatuses.PROCESSING));

    Integer pending = jdbc.queryForObject(
        "SELECT count(*) FROM payment.payment_outbox WHERE payment_id = ? AND status = 'PENDING'",
        Integer.class, paymentId);
    org.assertj.core.api.Assertions.assertThat(pending).isEqualTo(1);

    doReturn(transferFor(paymentId))
        .when(transferGateway).create(any(), any(), any(), any(), any(), any());
    outboxProcessor.processOne();

    Integer succeeded = jdbc.queryForObject(
        "SELECT count(*) FROM payment.payment_outbox "
            + "WHERE payment_id = ? AND status = 'SUCCEEDED'",
        Integer.class, paymentId);
    Integer paid = jdbc.queryForObject(
        "SELECT count(*) FROM payment.payments WHERE payment_id = ? AND status = 'PAID'",
        Integer.class, paymentId);
    Integer used = jdbc.queryForObject(
        "SELECT count(*) FROM payment.payment_qr_tokens "
            + "WHERE payment_id = ? AND used_at IS NOT NULL",
        Integer.class, paymentId);
    org.assertj.core.api.Assertions.assertThat(succeeded).isEqualTo(1);
    org.assertj.core.api.Assertions.assertThat(paid).isEqualTo(1);
    org.assertj.core.api.Assertions.assertThat(used).isEqualTo(1);
  }

  @Test
  void storesRetryableExternalFailureAndRetriesAfterSchedule() throws Exception {
    Session session = signup("outbox-retry@example.com");
    UUID paymentId = confirmPayment(session, createMerchant(session));
    when(transferGateway.create(any(), any(), any(), any(), any(), any()))
        .thenThrow(new StablecoinTransactionRemoteException(503,
            new RuntimeException("transaction server unavailable")));

    outboxProcessor.processOne();

    OutboxState failed = outboxState(paymentId);
    org.assertj.core.api.Assertions.assertThat(failed.status()).isEqualTo("FAILED");
    org.assertj.core.api.Assertions.assertThat(failed.attemptCount()).isGreaterThanOrEqualTo(1);
    org.assertj.core.api.Assertions.assertThat(failed.nextAttemptAt())
        .isAfter(OffsetDateTime.now());

    jdbc.update("UPDATE payment.payment_outbox SET next_attempt_at = now() WHERE payment_id = ?",
        paymentId);
    doReturn(transferFor(paymentId))
        .when(transferGateway).create(any(), any(), any(), any(), any(), any());

    outboxProcessor.processOne();

    org.assertj.core.api.Assertions.assertThat(outboxState(paymentId).status())
        .isEqualTo(PaymentOutboxStatuses.SUCCEEDED);
    Integer paid = jdbc.queryForObject(
        "SELECT count(*) FROM payment.payments WHERE payment_id = ? AND status = 'PAID'",
        Integer.class, paymentId);
    org.assertj.core.api.Assertions.assertThat(paid).isEqualTo(1);
  }

  @Test
  void recoversTransferFromWalletListAfterRemoteFailure() throws Exception {
    Session session = signup("outbox-recovery@example.com");
    UUID paymentId = confirmPayment(session, createMerchant(session));
    when(transferGateway.create(any(), any(), any(), any(), any(), any()))
        .thenThrow(new StablecoinTransactionRemoteException(503,
            new RuntimeException("ambiguous result")));
    doReturn(java.util.List.of(transferFor(paymentId)))
        .when(transferGateway).findByReference(any(), eq("payment_" + paymentId));

    outboxProcessor.processOne();

    org.assertj.core.api.Assertions.assertThat(outboxState(paymentId).status())
        .isEqualTo(PaymentOutboxStatuses.SUCCEEDED);
    Integer paid = jdbc.queryForObject(
        "SELECT count(*) FROM payment.payments WHERE payment_id = ? AND status = 'PAID'",
        Integer.class, paymentId);
    org.assertj.core.api.Assertions.assertThat(paid).isEqualTo(1);
    verify(transferGateway).findByReference(any(), eq("payment_" + paymentId));
  }

  @Test
  void marksPaymentFailedForNonRetryableProcessingError() throws Exception {
    Session session = signup("outbox-failed@example.com");
    UUID paymentId = confirmPayment(session, createMerchant(session));
    when(transferGateway.create(any(), any(), any(), any(), any(), any()))
        .thenThrow(new StablecoinTransactionRemoteException(400,
            new RuntimeException("invalid transfer")));

    outboxProcessor.processOne();

    String status = jdbc.queryForObject(
        "SELECT status FROM payment.payments WHERE payment_id = ?", String.class, paymentId);
    org.assertj.core.api.Assertions.assertThat(status).isEqualTo(PaymentStatuses.FAILED);
    org.assertj.core.api.Assertions.assertThat(outboxState(paymentId).status())
        .isEqualTo(PaymentOutboxStatuses.DEAD);
  }

  @Test
  void doesNotScheduleInternalApplicationFailureForRetry() throws Exception {
    Session session = signup("outbox-internal-failure@example.com");
    UUID paymentId = confirmPayment(session, createMerchant(session));
    when(transferGateway.create(any(), any(), any(), any(), any(), any()))
        .thenThrow(new InternalApplicationException());

    outboxProcessor.processOne();

    OutboxState failed = outboxState(paymentId);
    org.assertj.core.api.Assertions.assertThat(failed.status()).isEqualTo(PaymentOutboxStatuses.DEAD);
    org.assertj.core.api.Assertions.assertThat(failed.attemptCount()).isEqualTo(1);
    outboxProcessor.processOne();
    org.assertj.core.api.Assertions.assertThat(outboxState(paymentId).attemptCount()).isEqualTo(1);
  }

  @Test
  void resumesMerchantProvisioningWithoutCreatingDuplicateWallet() throws Exception {
    Session session = signup("merchant-resume@example.com");
    String response = mvc.perform(post("/v1/merchants")
            .with(jwt().jwt(jwt -> jwt.subject(session.userId().toString())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(new CreateMerchantRequest("Resume Merchant", "resume-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 20)))))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    UUID merchantId = UUID.fromString(objectMapper.readTree(response).get("merchant_id").asText());
    clearInvocations(walletProvisioner);

    doThrow(new StablecoinTransactionRemoteException(503, new RuntimeException("temporary")))
        .when(tokenAccounts).register(any(), any());
    merchantOutboxProcessor.processOne();
    verify(walletProvisioner, times(1)).create(any());

    jdbc.update("UPDATE merchant.merchant_outbox SET next_attempt_at = now() "
        + "WHERE merchant_id = ?", merchantId);
    doNothing().when(tokenAccounts).register(any(), any());
    merchantOutboxProcessor.processOne();

    verify(walletProvisioner, times(1)).create(any());
    Integer wallets = jdbc.queryForObject(
        "SELECT count(*) FROM merchant.merchant_wallets WHERE merchant_id = ?",
        Integer.class, merchantId);
    org.assertj.core.api.Assertions.assertThat(wallets).isEqualTo(1);
  }

  private UUID confirmPayment(Session session, UUID merchantId) throws Exception {
    JsonNode created = createPayment(session, merchantId);
    UUID paymentId = UUID.fromString(created.get("payment_id").asText());
    String rawToken = created.get("qr_payload").asText()
        .substring(created.get("qr_payload").asText().lastIndexOf('/') + 1);
    mvc.perform(post("/v1/payment-qr/{token}/confirm", rawToken)
            .with(jwt().jwt(jwt -> jwt.subject(session.userId().toString()))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(PaymentStatuses.PROCESSING));
    return paymentId;
  }

  private OutboxState outboxState(UUID paymentId) {
    return jdbc.queryForObject(
        "SELECT status, attempt_count, next_attempt_at FROM payment.payment_outbox "
            + "WHERE payment_id = ?",
        (rs, rowNum) -> new OutboxState(rs.getString("status"),
            rs.getInt("attempt_count"), rs.getObject("next_attempt_at", OffsetDateTime.class)),
        paymentId);
  }

  private TransferGateway.TransferResult transferFor(UUID paymentId) {
    return jdbc.queryForObject(
        "SELECT customer_wallet_id, merchant_wallet_id, token, amount FROM payment.payments "
            + "WHERE payment_id = ?",
        (rs, rowNum) -> new TransferGateway.TransferResult(
            UUID.randomUUID(), rs.getObject("customer_wallet_id", UUID.class),
            rs.getObject("merchant_wallet_id", UUID.class), rs.getString("token"),
            rs.getBigDecimal("amount").toBigInteger(), "payment_" + paymentId, "CONFIRMED"),
        paymentId);
  }

  private JsonNode createPayment(Session session, UUID merchantId) throws Exception {
    String response = mvc.perform(post("/v1/merchants/{merchantId}/payments", merchantId)
            .with(jwt().jwt(jwt -> jwt.subject(session.userId().toString())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(new CreatePaymentRequest(
                "outbox-order", "USDC-test", BigInteger.ONE, "outbox payment"))))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(response);
  }

  private Session signup(String email) throws Exception {
    String response = mvc.perform(post("/v1/user-auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(new SignupPayload(email, "password123", "Outbox user"))))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    AuthTokenResponse auth = objectMapper.readValue(response, AuthTokenResponse.class);
    return new Session(auth.user_id(), auth.access_token());
  }

  private UUID createMerchant(Session session) throws Exception {
    String response = mvc.perform(post("/v1/merchants")
            .with(jwt().jwt(jwt -> jwt.subject(session.userId().toString())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(new CreateMerchantRequest("Outbox Merchant", "ob-" + UUID.randomUUID()
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
  private record OutboxState(String status, int attemptCount, OffsetDateTime nextAttemptAt) {}
}
