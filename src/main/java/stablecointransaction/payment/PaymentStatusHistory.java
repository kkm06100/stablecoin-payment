package stablecointransaction.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_status_histories", schema = "payment")
public class PaymentStatusHistory {
  @Id
  @Column(name = "history_id")
  private UUID historyId;
  @Column(name = "payment_id", nullable = false)
  private UUID paymentId;
  @Column(name = "from_status")
  private String fromStatus;
  @Column(name = "to_status", nullable = false)
  private String toStatus;
  private String reason;
  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected PaymentStatusHistory() {}

  public PaymentStatusHistory(UUID historyId, UUID paymentId, String fromStatus,
                              String toStatus, String reason, OffsetDateTime createdAt) {
    this.historyId = historyId;
    this.paymentId = paymentId;
    this.fromStatus = fromStatus;
    this.toStatus = toStatus;
    this.reason = reason;
    this.createdAt = createdAt;
  }

  public UUID getHistoryId() { return historyId; }
  public UUID getPaymentId() { return paymentId; }
  public String getFromStatus() { return fromStatus; }
  public String getToStatus() { return toStatus; }
  public String getReason() { return reason; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
}
