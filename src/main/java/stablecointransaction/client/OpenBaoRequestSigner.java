package stablecointransaction.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenBaoRequestSigner implements StablecoinTransactionRequestSigner {
  private final RestClient client;
  private final String operatorId;
  private final String keyLabel;
  private final String mount;

  public OpenBaoRequestSigner(RestClient.Builder builder,
                             ObjectMapper objectMapper,
                             @Value("${bao.url}") String baoUrl,
                             @Value("${bao.token}") String baoToken,
                             @Value("${bao.transit-mount}") String mount,
                             @Value("${bao.operator-key-label}") String keyLabel,
                             @Value("${stablecoin.operator-id}") String operatorId) {
    this.client = builder.baseUrl(baoUrl)
        .defaultHeader("X-Vault-Token", baoToken).build();
    this.mount = mount;
    this.keyLabel = keyLabel;
    this.operatorId = operatorId;
  }

  @Override
  public SignedHeaders sign(String method, String pathAndQuery, byte[] body, String timestamp) {
    String canonical = operatorId + "\n" + method.toUpperCase() + "\n"
        + pathAndQuery + "\n" + timestamp + "\n" + sha256Hex(body);
    Map<?, ?> response = client.post()
        .uri("/v1/{mount}/sign/{label}", mount, keyLabel)
        .body(Map.of("input", Base64.getEncoder().encodeToString(
            canonical.getBytes(StandardCharsets.UTF_8))))
        .retrieve().body(Map.class);
    Map<?, ?> data = (Map<?, ?>) response.get("data");
    String signature = (String) data.get("signature");
    String rawBase64 = signature.substring(signature.lastIndexOf(':') + 1);
    return new SignedHeaders(operatorId,
        HexFormat.of().formatHex(Base64.getDecoder().decode(rawBase64)), timestamp);
  }

  private String sha256Hex(byte[] body) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body));
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }
}
