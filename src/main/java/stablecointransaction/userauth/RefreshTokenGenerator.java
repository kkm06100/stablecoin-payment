package stablecointransaction.userauth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import stablecointransaction.exception.InternalApplicationException;
import java.security.SecureRandom;
import java.util.Base64;
import stablecointransaction.common.crypto.CryptoAlgorithms;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenGenerator {
  private static final int TOKEN_BYTES = 32;
  private final SecureRandom secureRandom = new SecureRandom();

  public String generate() {
    byte[] bytes = new byte[TOKEN_BYTES];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public String hash(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance(CryptoAlgorithms.SHA_256);
      byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new InternalApplicationException(e);
    }
  }
}
