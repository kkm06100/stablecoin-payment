package stablecointransaction.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer_wallets", schema = "identity")
@IdClass(CustomerWalletId.class)
public class CustomerWallet {

  @Id
  @Column(name = "customer_id")
  private UUID customerId;

  @Id
  @Column(name = "wallet_id")
  private UUID walletId;

  @Column(name = "wallet_role", nullable = false)
  private String walletRole;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected CustomerWallet() {}

  public CustomerWallet(UUID customerId, UUID walletId, String walletRole,
                        OffsetDateTime createdAt) {
    this.customerId = customerId;
    this.walletId = walletId;
    this.walletRole = walletRole;
    this.createdAt = createdAt;
  }

  public UUID getCustomerId() { return customerId; }
  public UUID getWalletId() { return walletId; }
  public String getWalletRole() { return walletRole; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
}
