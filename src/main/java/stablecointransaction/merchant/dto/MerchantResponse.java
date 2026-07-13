package stablecointransaction.merchant.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.merchant.Merchant;

public record MerchantResponse(
    UUID merchant_id,
    String merchant_name,
    String business_number,
    String status,
    UUID settlement_wallet_id,
    OffsetDateTime created_at) {

  public static MerchantResponse from(Merchant merchant, UUID settlementWalletId) {
    return new MerchantResponse(merchant.getMerchantId(), merchant.getMerchantName(),
        merchant.getBusinessNumber(), merchant.getStatus(), settlementWalletId,
        merchant.getCreatedAt());
  }
}
