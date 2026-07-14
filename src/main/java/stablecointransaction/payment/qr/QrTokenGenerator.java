package stablecointransaction.payment.qr;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import stablecointransaction.common.crypto.CryptoAlgorithms;
import stablecointransaction.payment.PaymentProperties;
import stablecointransaction.exception.InternalApplicationException;
import org.springframework.stereotype.Component;

@Component
public class QrTokenGenerator {
  private final byte[] secret;

  public QrTokenGenerator(PaymentProperties properties) {
    this.secret = properties.getQrSecret().getBytes(StandardCharsets.UTF_8);
    if (secret.length < 32) {
      throw new InternalApplicationException();
    }
  }

  public String generate(UUID qrTokenId) {
    try {
      Mac mac = Mac.getInstance(CryptoAlgorithms.HMAC_SHA_256);
      mac.init(new SecretKeySpec(secret, CryptoAlgorithms.HMAC_SHA_256));
      byte[] bytes = mac.doFinal(qrTokenId.toString().getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new InternalApplicationException(e);
    }
  }
}
