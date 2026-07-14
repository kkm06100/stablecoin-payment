package stablecointransaction.merchant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "merchants", schema = "merchant")
public class Merchant {
  @Id
  @Column(name = "merchant_id")
  private UUID merchantId;

  @Column(name = "merchant_name", nullable = false)
  private String merchantName;

  @Column(name = "business_number")
  private String businessNumber;

  @Column(nullable = false)
  private String status;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected Merchant() {}

  public Merchant(UUID merchantId, String merchantName, String businessNumber,
                  String status, OffsetDateTime createdAt) {
    this.merchantId = merchantId;
    this.merchantName = merchantName;
    this.businessNumber = businessNumber;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = createdAt;
  }

  public UUID getMerchantId() { return merchantId; }
  public String getMerchantName() { return merchantName; }
  public String getBusinessNumber() { return businessNumber; }
  public String getStatus() { return status; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }

  public void activate(OffsetDateTime now) {
    this.status = MerchantStatuses.ACTIVE;
    this.updatedAt = now;
  }
}
