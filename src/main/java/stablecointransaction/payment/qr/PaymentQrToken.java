package stablecointransaction.payment.qr;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_qr_tokens", schema = "payment")
public class PaymentQrToken {
  @Id
  @Column(name = "qr_token_id")
  private UUID qrTokenId;
  @Column(name = "payment_id", nullable = false)
  private UUID paymentId;
  @Column(name = "token_hash", nullable = false, unique = true)
  private String tokenHash;
  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;
  @Column(name = "used_at")
  private OffsetDateTime usedAt;
  @Column(name = "revoked_at")
  private OffsetDateTime revokedAt;
  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected PaymentQrToken() {}

  public PaymentQrToken(UUID qrTokenId, UUID paymentId, String tokenHash,
                        OffsetDateTime expiresAt, OffsetDateTime createdAt) {
    this.qrTokenId = qrTokenId;
    this.paymentId = paymentId;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
  }

  public UUID getQrTokenId() { return qrTokenId; }
  public UUID getPaymentId() { return paymentId; }
  public String getTokenHash() { return tokenHash; }
  public OffsetDateTime getExpiresAt() { return expiresAt; }
  public OffsetDateTime getUsedAt() { return usedAt; }
  public OffsetDateTime getRevokedAt() { return revokedAt; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
}
