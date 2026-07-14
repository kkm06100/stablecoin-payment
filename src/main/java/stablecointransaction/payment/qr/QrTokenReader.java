package stablecointransaction.payment.qr;

import java.time.OffsetDateTime;
import stablecointransaction.payment.exception.InvalidQrTokenException;
import stablecointransaction.payment.exception.PaymentExpiredException;
import org.springframework.stereotype.Component;

@Component
public class QrTokenReader {
  private final PaymentQrTokenRepository tokens;
  private final QrTokenHasher hasher;

  public QrTokenReader(PaymentQrTokenRepository tokens, QrTokenHasher hasher) {
    this.tokens = tokens;
    this.hasher = hasher;
  }

  public PaymentQrToken requireActive(String rawToken, OffsetDateTime now) {
    PaymentQrToken token = tokens.findByTokenHash(hasher.hash(rawToken))
        .orElseThrow(InvalidQrTokenException::new);
    if (token.getRevokedAt() != null || token.getUsedAt() != null) {
      throw new InvalidQrTokenException();
    }
    if (!token.getExpiresAt().isAfter(now)) {
      throw new PaymentExpiredException();
    }
    return token;
  }

  public PaymentQrToken findByRawToken(String rawToken) {
    return tokens.findByTokenHash(hasher.hash(rawToken))
        .orElseThrow(InvalidQrTokenException::new);
  }
}
