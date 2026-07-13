package stablecointransaction.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerWalletRepository
    extends JpaRepository<CustomerWallet, CustomerWalletId> {
  Optional<CustomerWallet> findByCustomerIdAndWalletRole(UUID customerId, String walletRole);
  Optional<CustomerWallet> findByWalletId(UUID walletId);
}
