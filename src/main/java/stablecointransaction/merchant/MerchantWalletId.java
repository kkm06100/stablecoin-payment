package stablecointransaction.merchant;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class MerchantWalletId implements Serializable {
  private UUID merchantId;
  private UUID walletId;

  protected MerchantWalletId() {}

  public MerchantWalletId(UUID merchantId, UUID walletId) {
    this.merchantId = merchantId;
    this.walletId = walletId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MerchantWalletId that)) return false;
    return Objects.equals(merchantId, that.merchantId)
        && Objects.equals(walletId, that.walletId);
  }

  @Override
  public int hashCode() { return Objects.hash(merchantId, walletId); }
}
