package stablecointransaction.merchant;

import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.api.dto.WalletResponse;
import stablecointransaction.merchant.dto.MerchantResponse;
import stablecointransaction.user.User;
import stablecointransaction.user.UserRepository;
import stablecointransaction.user.UserStatus;
import stablecointransaction.wallet.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantService {
  private final MerchantRepository merchants;
  private final MerchantMemberRepository members;
  private final MerchantWalletRepository merchantWallets;
  private final UserRepository users;
  private final WalletService walletService;

  public MerchantService(MerchantRepository merchants,
                         MerchantMemberRepository members,
                         MerchantWalletRepository merchantWallets,
                         UserRepository users,
                         WalletService walletService) {
    this.merchants = merchants;
    this.members = members;
    this.merchantWallets = merchantWallets;
    this.users = users;
    this.walletService = walletService;
  }

  @Transactional
  public MerchantResponse create(UUID ownerUserId, String merchantName,
                                 String businessNumber) {
    User owner = users.findById(ownerUserId)
        .orElseThrow(() -> new MerchantAccessDeniedException("user not found: " + ownerUserId));
    if (owner.getStatus() != UserStatus.ACTIVE) {
      throw new MerchantAccessDeniedException("user is not active: " + ownerUserId);
    }

    OffsetDateTime now = OffsetDateTime.now();
    UUID merchantId = UUID.randomUUID();
    Merchant merchant = merchants.save(new Merchant(merchantId, merchantName.trim(),
        normalizeBusinessNumber(businessNumber), MerchantStatuses.ACTIVE, now));
    members.save(new MerchantMember(merchantId, ownerUserId, MerchantRoles.OWNER,
        MerchantMemberStatuses.ACTIVE, now));

    WalletResponse wallet = walletService.createUserWallet("merchant-" + merchantId);
    merchantWallets.save(new MerchantWallet(merchantId, wallet.wallet_id(),
        MerchantWalletRoles.SETTLEMENT, MerchantWalletStatuses.ACTIVE, now));
    return MerchantResponse.from(merchant, wallet.wallet_id());
  }

  @Transactional(readOnly = true)
  public MerchantResponse get(UUID userId, UUID merchantId) {
    Merchant merchant = merchants.findById(merchantId)
        .orElseThrow(() -> new MerchantNotFoundException("merchant " + merchantId));
    members.findByMerchantIdAndUserId(merchantId, userId)
        .filter(member -> MerchantMemberStatuses.ACTIVE.equals(member.getStatus()))
        .orElseThrow(() -> new MerchantAccessDeniedException(
            "user cannot access merchant " + merchantId));
    MerchantWallet wallet = merchantWallets.findByMerchantIdAndWalletRoleAndStatus(
            merchantId, MerchantWalletRoles.SETTLEMENT, MerchantWalletStatuses.ACTIVE)
        .orElseThrow(() -> new MerchantInactiveException(
            "active settlement wallet not found: " + merchantId));
    return MerchantResponse.from(merchant, wallet.getWalletId());
  }

  private String normalizeBusinessNumber(String businessNumber) {
    if (businessNumber == null || businessNumber.isBlank()) return null;
    return businessNumber.replaceAll("[^0-9]", "");
  }
}
