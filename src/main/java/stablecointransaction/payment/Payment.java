package stablecointransaction.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments", schema = "payment")
public class Payment {
  @Id
  @Column(name = "payment_id")
  private UUID paymentId;
  @Column(name = "merchant_id", nullable = false)
  private UUID merchantId;
  @Column(name = "merchant_wallet_id", nullable = false)
  private UUID merchantWalletId;
  @Column(name = "created_by", nullable = false)
  private UUID createdBy;
  @Column(name = "order_id", nullable = false)
  private String orderId;
  @Column(nullable = false)
  private String token;
  @Column(nullable = false, precision = 40, scale = 0)
  private BigInteger amount;
  @Column
  private String description;
  @Column(nullable = false)
  private String status;
  @Column(name = "customer_id")
  private UUID customerId;
  @Column(name = "customer_wallet_id")
  private UUID customerWalletId;
  @Column(name = "transfer_id", unique = true)
  private UUID transferId;
  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;
  @Column(name = "processing_at")
  private OffsetDateTime processingAt;
  @Column(name = "paid_at")
  private OffsetDateTime paidAt;
  @Column(name = "cancelled_at")
  private OffsetDateTime cancelledAt;
  @Column(name = "failure_code")
  private String failureCode;
  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected Payment() {}

  public Payment(UUID paymentId, UUID merchantId, UUID merchantWalletId,
                 UUID createdBy, String orderId, String token, BigInteger amount,
                 String description, String status, OffsetDateTime expiresAt,
                 OffsetDateTime createdAt) {
    this.paymentId = paymentId;
    this.merchantId = merchantId;
    this.merchantWalletId = merchantWalletId;
    this.createdBy = createdBy;
    this.orderId = orderId;
    this.token = token;
    this.amount = amount;
    this.description = description;
    this.status = status;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
    this.updatedAt = createdAt;
  }

  public UUID getPaymentId() { return paymentId; }
  public UUID getMerchantId() { return merchantId; }
  public UUID getMerchantWalletId() { return merchantWalletId; }
  public UUID getCreatedBy() { return createdBy; }
  public String getOrderId() { return orderId; }
  public String getToken() { return token; }
  public BigInteger getAmount() { return amount; }
  public String getDescription() { return description; }
  public String getStatus() { return status; }
  public UUID getCustomerId() { return customerId; }
  public UUID getCustomerWalletId() { return customerWalletId; }
  public UUID getTransferId() { return transferId; }
  public OffsetDateTime getExpiresAt() { return expiresAt; }
  public OffsetDateTime getProcessingAt() { return processingAt; }
  public OffsetDateTime getPaidAt() { return paidAt; }
  public OffsetDateTime getCancelledAt() { return cancelledAt; }
  public String getFailureCode() { return failureCode; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
