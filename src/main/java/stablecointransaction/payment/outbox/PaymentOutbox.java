package stablecointransaction.payment.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "payment_outbox", schema = "payment")
public class PaymentOutbox {
  @Id
  @Column(name = "event_id")
  private UUID eventId;
  @Column(name = "payment_id", nullable = false)
  private UUID paymentId;
  @Column(name = "qr_token_id", nullable = false)
  private UUID qrTokenId;
  @Column(name = "event_type", nullable = false)
  private String eventType;
  @Column(name = "idempotency_key", nullable = false, unique = true)
  private String idempotencyKey;
  @Column(nullable = false, columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String payload;
  @Column(nullable = false)
  private String status;
  @Column(name = "attempt_count", nullable = false)
  private int attemptCount;
  @Column(name = "next_attempt_at", nullable = false)
  private OffsetDateTime nextAttemptAt;
  @Column(name = "locked_at")
  private OffsetDateTime lockedAt;
  @Column(name = "external_transfer_id")
  private UUID externalTransferId;
  @Column(name = "last_error")
  private String lastError;
  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected PaymentOutbox() {}

  public PaymentOutbox(UUID eventId, UUID paymentId, UUID qrTokenId, String eventType,
                       String idempotencyKey, String payload, OffsetDateTime now) {
    this.eventId = eventId;
    this.paymentId = paymentId;
    this.qrTokenId = qrTokenId;
    this.eventType = eventType;
    this.idempotencyKey = idempotencyKey;
    this.payload = payload;
    this.status = PaymentOutboxStatuses.PENDING;
    this.attemptCount = 0;
    this.nextAttemptAt = now;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public UUID getEventId() { return eventId; }
  public UUID getPaymentId() { return paymentId; }
  public UUID getQrTokenId() { return qrTokenId; }
  public String getStatus() { return status; }
  public int getAttemptCount() { return attemptCount; }
  public OffsetDateTime getNextAttemptAt() { return nextAttemptAt; }
  public UUID getExternalTransferId() { return externalTransferId; }

  public void markProcessing(OffsetDateTime now) {
    status = PaymentOutboxStatuses.PROCESSING;
    lockedAt = now;
    attemptCount++;
    updatedAt = now;
  }

  public void markSucceeded(UUID transferId, OffsetDateTime now) {
    status = PaymentOutboxStatuses.SUCCEEDED;
    externalTransferId = transferId;
    lastError = null;
    lockedAt = null;
    updatedAt = now;
  }

  public void markFailed(String error, OffsetDateTime nextAttemptAt, OffsetDateTime now) {
    status = PaymentOutboxStatuses.FAILED;
    lastError = error;
    this.nextAttemptAt = nextAttemptAt;
    lockedAt = null;
    updatedAt = now;
  }

  public void markDead(String error, OffsetDateTime now) {
    status = PaymentOutboxStatuses.DEAD;
    lastError = error;
    lockedAt = null;
    updatedAt = now;
  }
}
