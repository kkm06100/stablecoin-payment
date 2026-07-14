package stablecointransaction.external.adapter;

import stablecointransaction.external.exception.StablecoinTransactionRemoteException;
import stablecointransaction.exception.InternalApplicationException;
import stablecointransaction.external.StablecoinTransactionClientProperties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;
import stablecointransaction.external.port.TokenAccountRegistrar;
import stablecointransaction.external.port.TransferGateway;
import stablecointransaction.external.port.WalletProvisioner;
import stablecointransaction.external.port.WalletReader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

@Component
public class StablecoinTransactionRestClient {
  private static final String OPERATOR_ID = "x-nw-operator-id";
  private static final String TIMESTAMP = "x-nw-timestamp";
  private static final String SIGNATURE = "x-nw-signature";

  private final RestClient client;
  private final ObjectMapper objectMapper;
  private final StablecoinTransactionRequestSigner signer;

  public StablecoinTransactionRestClient(RestClient.Builder builder,
                                         ObjectMapper objectMapper,
                                         StablecoinTransactionClientProperties properties,
                                         StablecoinTransactionRequestSigner signer) {
    this.client = builder.baseUrl(properties.getBaseUrl()).build();
    this.objectMapper = objectMapper;
    this.signer = signer;
  }

  public WalletProvisioner.ProvisionedWallet create(String label) {
    String body = json(new CreateWalletBody(label));
    return exchange("POST", "/v1/wallets", body, WalletProvisioner.ProvisionedWallet.class);
  }

  public WalletReader.WalletDetails get(UUID walletId) {
    return exchange("GET", "/v1/wallets/" + walletId, "", WalletReader.WalletDetails.class);
  }

  public void register(UUID walletId, String mint) {
    String body = json(new RegisterTokenAccountBody(mint));
    exchange("POST", "/v1/wallets/" + walletId + "/token-accounts", body, Object.class);
  }

  public TransferGateway.TransferResult create(UUID srcWalletId, UUID dstWalletId,
                                               String token, BigInteger amount,
                                               String referenceId, String memo) {
    String body = json(new CreateTransferBody(srcWalletId, dstWalletId, token,
        amount, referenceId, memo));
    return exchange("POST", "/v1/transfers", body, TransferGateway.TransferResult.class);
  }

  public TransferGateway.TransferResult getTransfer(UUID transferId) {
    return exchange("GET", "/v1/transfers/" + transferId, "", TransferGateway.TransferResult.class);
  }

  public List<TransferGateway.TransferResult> findTransfers(UUID walletId) {
    String path = "/v1/transfers?wallet_id=" + walletId;
    TransferGateway.TransferResult[] transfers = exchange(
        "GET", path, "", TransferGateway.TransferResult[].class);
    return transfers == null ? List.of() : Arrays.asList(transfers);
  }

  private <T> T exchange(String method, String path, String body, Class<T> type) {
    String timestamp = Long.toString(System.currentTimeMillis());
    StablecoinTransactionRequestSigner.SignedHeaders headers = signer.sign(
        method, path, body.getBytes(StandardCharsets.UTF_8), timestamp);
    try {
      return client.method(org.springframework.http.HttpMethod.valueOf(method))
          .uri(path)
          .headers(http -> {
            http.set(OPERATOR_ID, headers.operatorId());
            http.set(TIMESTAMP, headers.timestamp());
            http.set(SIGNATURE, headers.signatureHex());
          })
          .contentType(MediaType.APPLICATION_JSON)
          .body(body)
          .retrieve()
          .body(type);
    } catch (RestClientResponseException e) {
      throw new StablecoinTransactionRemoteException(e.getStatusCode().value(),
          remoteCode(e.getResponseBodyAsString()), e);
    } catch (ResourceAccessException e) {
      throw new StablecoinTransactionRemoteException(503, e);
    }
  }

  private String json(Object body) {
    try {
      return objectMapper.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      throw new InternalApplicationException(e);
    }
  }

  private String remoteCode(String body) {
    if (body == null || body.isBlank()) return null;
    try {
      var node = objectMapper.readTree(body);
      var code = node.get("code");
      return code == null || !code.isTextual() ? null : code.asText();
    } catch (JsonProcessingException ignored) {
      return null;
    }
  }

  private record CreateWalletBody(String label) {}

  private record CreateTransferBody(UUID src_wallet_id, UUID dst_wallet_id,
                                    String token, BigInteger amount,
                                    String reference_id, String memo) {}

  private record RegisterTokenAccountBody(String mint) {}
}
