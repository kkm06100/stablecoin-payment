package stablecointransaction.merchant.service;

import java.util.UUID;
import stablecointransaction.merchant.*;
import stablecointransaction.merchant.dto.MerchantResponse;
import stablecointransaction.merchant.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantQueryService {
  private final MerchantRepository merchants;
  private final MerchantMemberRepository members;
  private final MerchantWalletRepository wallets;

  public MerchantQueryService(MerchantRepository merchants, MerchantMemberRepository members,
                              MerchantWalletRepository wallets) {
    this.merchants = merchants;
    this.members = members;
    this.wallets = wallets;
  }

  @Transactional(readOnly = true)
  public MerchantResponse get(UUID userId, UUID merchantId) {
    Merchant merchant = merchants.findById(merchantId)
        .orElseThrow(MerchantNotFoundException::new);
    members.findByMerchantIdAndUserId(merchantId, userId)
        .filter(member -> MerchantMemberStatuses.ACTIVE.equals(member.getStatus()))
        .orElseThrow(MerchantAccessDeniedException::new);
    MerchantWallet wallet = wallets.findByMerchantIdAndWalletRoleAndStatus(
        merchantId, MerchantWalletRoles.SETTLEMENT, MerchantWalletStatuses.ACTIVE)
        .orElseThrow(MerchantInactiveException::new);
    return MerchantResponse.from(merchant, wallet.getWalletId());
  }
}
