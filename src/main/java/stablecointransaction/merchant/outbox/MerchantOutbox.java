package stablecointransaction.merchant.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "merchant_outbox", schema = "merchant")
public class MerchantOutbox {
  @Id
  @Column(name = "event_id")
  private UUID eventId;
  @Column(name = "merchant_id", nullable = false)
  private UUID merchantId;
  @Column(name = "event_type", nullable = false)
  private String eventType;
  @Column(name = "idempotency_key", nullable = false, unique = true)
  private String idempotencyKey;
  @Column(nullable = false)
  private String status;
  @Column(name = "attempt_count", nullable = false)
  private int attemptCount;
  @Column(name = "next_attempt_at", nullable = false)
  private OffsetDateTime nextAttemptAt;
  @Column(name = "locked_at")
  private OffsetDateTime lockedAt;
  @Column(name = "wallet_id")
  private UUID walletId;
  @Column(name = "last_error")
  private String lastError;
  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected MerchantOutbox() {}

  public MerchantOutbox(UUID eventId, UUID merchantId, String eventType,
                        String idempotencyKey, OffsetDateTime now) {
    this.eventId = eventId;
    this.merchantId = merchantId;
    this.eventType = eventType;
    this.idempotencyKey = idempotencyKey;
    this.status = MerchantOutboxStatuses.PENDING;
    this.nextAttemptAt = now;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public UUID getMerchantId() { return merchantId; }
  public UUID getWalletId() { return walletId; }
  public String getStatus() { return status; }
  public int getAttemptCount() { return attemptCount; }

  public void markProcessing(OffsetDateTime now) {
    status = MerchantOutboxStatuses.PROCESSING;
    lockedAt = now;
    attemptCount++;
    updatedAt = now;
  }

  public void markSucceeded(UUID walletId, OffsetDateTime now) {
    status = MerchantOutboxStatuses.SUCCEEDED;
    this.walletId = walletId;
    lockedAt = null;
    lastError = null;
    updatedAt = now;
  }

  public void markFailed(String error, OffsetDateTime nextAttemptAt, OffsetDateTime now) {
    status = MerchantOutboxStatuses.FAILED;
    lastError = error;
    this.nextAttemptAt = nextAttemptAt;
    lockedAt = null;
    updatedAt = now;
  }

  public void markWalletProvisioned(UUID walletId, OffsetDateTime now) {
    this.walletId = walletId;
    this.updatedAt = now;
  }

  public void markDead(String error, OffsetDateTime now) {
    status = MerchantOutboxStatuses.DEAD;
    lastError = error;
    lockedAt = null;
    updatedAt = now;
  }
}
