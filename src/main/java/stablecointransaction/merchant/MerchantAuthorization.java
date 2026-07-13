package stablecointransaction.merchant;

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
        .orElseThrow(() -> new MerchantAccessDeniedException(
            "user is not a merchant member: " + userId));
    if (!MerchantRoles.PAYMENT_CREATORS.contains(member.getMemberRole())) {
      throw new MerchantAccessDeniedException(
          "member cannot create payment for merchant " + merchantId);
    }
    return merchant;
  }

  @Transactional(readOnly = true)
  public Merchant requireMembership(UUID userId, UUID merchantId) {
    Merchant merchant = merchants.findById(merchantId)
        .orElseThrow(() -> new MerchantNotFoundException("merchant " + merchantId));
    if (!MerchantStatuses.ACTIVE.equals(merchant.getStatus())) {
      throw new MerchantInactiveException("merchant is not active: " + merchantId);
    }

    MerchantMember member = members.findByMerchantIdAndUserId(merchantId, userId)
        .orElseThrow(() -> new MerchantAccessDeniedException(
            "user is not a merchant member: " + userId));
    if (!MerchantMemberStatuses.ACTIVE.equals(member.getStatus())) {
      throw new MerchantAccessDeniedException(
          "merchant membership is not active: " + merchantId);
    }
    return merchant;
  }
}
