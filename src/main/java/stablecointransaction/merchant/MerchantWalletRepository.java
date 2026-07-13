package stablecointransaction.merchant;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantWalletRepository
    extends JpaRepository<MerchantWallet, MerchantWalletId> {
  Optional<MerchantWallet> findByMerchantIdAndWalletRoleAndStatus(
      UUID merchantId, String walletRole, String status);
  Optional<MerchantWallet> findByWalletId(UUID walletId);
}
