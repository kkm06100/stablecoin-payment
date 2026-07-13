package stablecointransaction.payment.qr;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import stablecointransaction.common.crypto.CryptoAlgorithms;
import stablecointransaction.payment.PaymentProperties;
import org.springframework.stereotype.Component;

@Component
public class PaymentQrService {
  private final PaymentQrTokenRepository tokens;
  private final byte[] secret;

  public PaymentQrService(PaymentQrTokenRepository tokens, PaymentProperties properties) {
    this.tokens = tokens;
    this.secret = properties.getQrSecret().getBytes(StandardCharsets.UTF_8);
    if (secret.length < 32) {
      throw new IllegalStateException("payment.qr-secret must be at least 32 bytes");
    }
  }

  public IssuedQr issueOrReuse(UUID paymentId, OffsetDateTime expiresAt, OffsetDateTime now) {
    return tokens
        .findFirstByPaymentIdAndRevokedAtIsNullAndUsedAtIsNullOrderByCreatedAtDesc(paymentId)
        .filter(token -> token.getExpiresAt().isAfter(now))
        .map(token -> new IssuedQr(derive(token.getQrTokenId()), token.getExpiresAt()))
        .orElseGet(() -> issue(paymentId, expiresAt, now));
  }

  private IssuedQr issue(UUID paymentId, OffsetDateTime expiresAt, OffsetDateTime now) {
    UUID qrTokenId = UUID.randomUUID();
    String rawToken = derive(qrTokenId);
    tokens.save(new PaymentQrToken(qrTokenId, paymentId, hash(rawToken),
        expiresAt, now));
    return new IssuedQr(rawToken, expiresAt);
  }

  public String hash(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance(CryptoAlgorithms.SHA_256);
      byte[] encoded = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(encoded);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }

  private String derive(UUID qrTokenId) {
    try {
      Mac mac = Mac.getInstance(CryptoAlgorithms.HMAC_SHA_256);
      mac.init(new SecretKeySpec(secret, CryptoAlgorithms.HMAC_SHA_256));
      byte[] bytes = mac.doFinal(qrTokenId.toString().getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new IllegalStateException("HmacSHA256 unavailable", e);
    }
  }

  public record IssuedQr(String rawToken, OffsetDateTime expiresAt) {}
}
