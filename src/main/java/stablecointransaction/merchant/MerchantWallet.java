package stablecointransaction.merchant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "merchant_wallets", schema = "merchant")
@IdClass(MerchantWalletId.class)
public class MerchantWallet {
  @Id
  @Column(name = "merchant_id")
  private UUID merchantId;

  @Id
  @Column(name = "wallet_id")
  private UUID walletId;

  @Column(name = "wallet_role", nullable = false)
  private String walletRole;

  @Column(nullable = false)
  private String status;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected MerchantWallet() {}

  public MerchantWallet(UUID merchantId, UUID walletId, String walletRole,
                        String status, OffsetDateTime createdAt) {
    this.merchantId = merchantId;
    this.walletId = walletId;
    this.walletRole = walletRole;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = createdAt;
  }

  public UUID getMerchantId() { return merchantId; }
  public UUID getWalletId() { return walletId; }
  public String getWalletRole() { return walletRole; }
  public String getStatus() { return status; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
