package stablecointransaction.user;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class CustomerWalletId implements Serializable {
  private UUID customerId;
  private UUID walletId;

  protected CustomerWalletId() {}

  public CustomerWalletId(UUID customerId, UUID walletId) {
    this.customerId = customerId;
    this.walletId = walletId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CustomerWalletId that)) return false;
    return Objects.equals(customerId, that.customerId)
        && Objects.equals(walletId, that.walletId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(customerId, walletId);
  }
}
