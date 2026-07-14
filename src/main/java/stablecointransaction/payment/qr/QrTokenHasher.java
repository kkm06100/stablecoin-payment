package stablecointransaction.payment.qr;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import stablecointransaction.common.crypto.CryptoAlgorithms;
import stablecointransaction.exception.InternalApplicationException;
import org.springframework.stereotype.Component;

@Component
public class QrTokenHasher {
  public String hash(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance(CryptoAlgorithms.SHA_256);
      byte[] encoded = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(encoded);
    } catch (NoSuchAlgorithmException e) {
      throw new InternalApplicationException(e);
    }
  }
}
