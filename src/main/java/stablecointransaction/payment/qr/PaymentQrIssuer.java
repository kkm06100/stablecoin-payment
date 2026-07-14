package stablecointransaction.payment.qr;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PaymentQrIssuer {
  private final PaymentQrTokenRepository tokens;
  private final QrTokenGenerator generator;
  private final QrTokenHasher hasher;

  public PaymentQrIssuer(PaymentQrTokenRepository tokens,
                         QrTokenGenerator generator,
                         QrTokenHasher hasher) {
    this.tokens = tokens;
    this.generator = generator;
    this.hasher = hasher;
  }

  public IssuedQr issueOrReuse(UUID paymentId, OffsetDateTime expiresAt,
                               OffsetDateTime now) {
    return tokens
        .findFirstByPaymentIdAndRevokedAtIsNullAndUsedAtIsNullOrderByCreatedAtDesc(paymentId)
        .filter(token -> token.getExpiresAt().isAfter(now))
        .map(token -> new IssuedQr(generator.generate(token.getQrTokenId()), token.getExpiresAt()))
        .orElseGet(() -> issue(paymentId, expiresAt, now));
  }

  private IssuedQr issue(UUID paymentId, OffsetDateTime expiresAt, OffsetDateTime now) {
    UUID qrTokenId = UUID.randomUUID();
    String rawToken = generator.generate(qrTokenId);
    tokens.save(new PaymentQrToken(qrTokenId, paymentId, hasher.hash(rawToken), expiresAt, now));
    return new IssuedQr(rawToken, expiresAt);
  }

  public record IssuedQr(String rawToken, OffsetDateTime expiresAt) {}
}
