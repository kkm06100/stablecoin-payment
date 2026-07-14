package stablecointransaction.merchant;

import stablecointransaction.merchant.exception.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.client.StablecoinTransactionClient;
import stablecointransaction.client.StablecoinTransactionClientProperties;
import stablecointransaction.merchant.dto.MerchantResponse;
import stablecointransaction.user.User;
import stablecointransaction.user.UserRepository;
import stablecointransaction.user.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantService {
  private final MerchantRepository merchants;
  private final MerchantMemberRepository members;
  private final MerchantWalletRepository merchantWallets;
  private final UserRepository users;
  private final StablecoinTransactionClient transactionClient;
  private final StablecoinTransactionClientProperties transactionProperties;

  public MerchantService(MerchantRepository merchants,
                         MerchantMemberRepository members,
                         MerchantWalletRepository merchantWallets,
                         UserRepository users,
                         StablecoinTransactionClient transactionClient,
                         StablecoinTransactionClientProperties transactionProperties) {
    this.merchants = merchants;
    this.members = members;
    this.merchantWallets = merchantWallets;
    this.users = users;
    this.transactionClient = transactionClient;
    this.transactionProperties = transactionProperties;
  }

  @Transactional
  public MerchantResponse create(UUID ownerUserId, String merchantName,
                                 String businessNumber) {
    User owner = users.findById(ownerUserId)
        .orElseThrow(MerchantAccessDeniedException::new);
    if (owner.getStatus() != UserStatus.ACTIVE) {
      throw new MerchantAccessDeniedException();
    }

    String normalizedBusinessNumber = normalizeBusinessNumber(businessNumber);
    if (normalizedBusinessNumber != null
        && merchants.findByBusinessNumber(normalizedBusinessNumber).isPresent()) {
      throw new MerchantAlreadyExistsException();
    }
    OffsetDateTime now = OffsetDateTime.now();
    UUID merchantId = UUID.randomUUID();
    Merchant merchant = merchants.save(new Merchant(merchantId, merchantName.trim(),
        normalizedBusinessNumber, MerchantStatuses.ACTIVE, now));
    members.save(new MerchantMember(merchantId, ownerUserId, MerchantRoles.OWNER,
        MerchantMemberStatuses.ACTIVE, now));

    StablecoinTransactionClient.RemoteWallet wallet = transactionClient.createUserWallet(
        "merchant-" + merchantId);
    transactionClient.registerTokenAccount(wallet.walletId(), transactionProperties.getUsdcTestMint());
    merchantWallets.save(new MerchantWallet(merchantId, wallet.walletId(),
        MerchantWalletRoles.SETTLEMENT, MerchantWalletStatuses.ACTIVE, now));
    return MerchantResponse.from(merchant, wallet.walletId());
  }

  @Transactional(readOnly = true)
  public MerchantResponse get(UUID userId, UUID merchantId) {
    Merchant merchant = merchants.findById(merchantId)
        .orElseThrow(MerchantNotFoundException::new);
    members.findByMerchantIdAndUserId(merchantId, userId)
        .filter(member -> MerchantMemberStatuses.ACTIVE.equals(member.getStatus()))
        .orElseThrow(MerchantAccessDeniedException::new);
    MerchantWallet wallet = merchantWallets.findByMerchantIdAndWalletRoleAndStatus(
            merchantId, MerchantWalletRoles.SETTLEMENT, MerchantWalletStatuses.ACTIVE)
        .orElseThrow(MerchantInactiveException::new);
    return MerchantResponse.from(merchant, wallet.getWalletId());
  }

  private String normalizeBusinessNumber(String businessNumber) {
    if (businessNumber == null || businessNumber.isBlank()) return null;
    return businessNumber.replaceAll("[^0-9]", "");
  }
}
