package stablecointransaction.payment.component;

import java.util.UUID;
import stablecointransaction.merchant.MerchantAuthorization;
import stablecointransaction.merchant.MerchantWallet;
import stablecointransaction.merchant.MerchantWalletRepository;
import stablecointransaction.merchant.MerchantWalletRoles;
import stablecointransaction.merchant.MerchantWalletStatuses;
import stablecointransaction.merchant.exception.MerchantInactiveException;
import org.springframework.stereotype.Component;

@Component
public class PaymentCreationContextResolver {
  private final MerchantAuthorization authorization;
  private final MerchantWalletRepository wallets;

  public PaymentCreationContextResolver(MerchantAuthorization authorization,
                                        MerchantWalletRepository wallets) {
    this.authorization = authorization;
    this.wallets = wallets;
  }

  public MerchantWallet resolve(UUID userId, UUID merchantId) {
    authorization.requirePaymentCreation(userId, merchantId);
    return wallets.findByMerchantIdAndWalletRoleAndStatus(
            merchantId, MerchantWalletRoles.SETTLEMENT, MerchantWalletStatuses.ACTIVE)
        .orElseThrow(MerchantInactiveException::new);
  }
}
