package stablecointransaction.merchant;

import stablecointransaction.merchant.exception.*;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MerchantAuthorization {
  private final MerchantRepository merchants;
  private final MerchantMemberRepository members;

  public MerchantAuthorization(MerchantRepository merchants,
                               MerchantMemberRepository members) {
    this.merchants = merchants;
    this.members = members;
  }

  @Transactional(readOnly = true)
  public Merchant requirePaymentCreation(UUID userId, UUID merchantId) {
    Merchant merchant = requireMembership(userId, merchantId);
    MerchantMember member = members.findByMerchantIdAndUserId(merchantId, userId)
        .orElseThrow(MerchantAccessDeniedException::new);
    if (!MerchantRoles.PAYMENT_CREATORS.contains(member.getMemberRole())) {
      throw new MerchantAccessDeniedException();
    }
    return merchant;
  }

  @Transactional(readOnly = true)
  public Merchant requireMembership(UUID userId, UUID merchantId) {
    Merchant merchant = merchants.findById(merchantId)
        .orElseThrow(MerchantNotFoundException::new);
    if (!MerchantStatuses.ACTIVE.equals(merchant.getStatus())) {
      throw new MerchantInactiveException();
    }

    MerchantMember member = members.findByMerchantIdAndUserId(merchantId, userId)
        .orElseThrow(MerchantAccessDeniedException::new);
    if (!MerchantMemberStatuses.ACTIVE.equals(member.getStatus())) {
      throw new MerchantAccessDeniedException();
    }
    return merchant;
  }
}
