package stablecointransaction.merchant;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class MerchantMemberId implements Serializable {
  private UUID merchantId;
  private UUID userId;

  protected MerchantMemberId() {}

  public MerchantMemberId(UUID merchantId, UUID userId) {
    this.merchantId = merchantId;
    this.userId = userId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MerchantMemberId that)) return false;
    return Objects.equals(merchantId, that.merchantId)
        && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() { return Objects.hash(merchantId, userId); }
}
