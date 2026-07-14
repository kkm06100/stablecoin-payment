package stablecointransaction.user;

import java.util.UUID;
import stablecointransaction.client.StablecoinTransactionClient;
import stablecointransaction.user.exception.CustomerNotFoundException;
import stablecointransaction.user.exception.CustomerWalletNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerWalletService {
  private final CustomerProfileRepository customers;
  private final CustomerWalletRepository wallets;
  private final StablecoinTransactionClient transactionClient;

  public CustomerWalletService(CustomerProfileRepository customers,
                               CustomerWalletRepository wallets,
                               StablecoinTransactionClient transactionClient) {
    this.customers = customers;
    this.wallets = wallets;
    this.transactionClient = transactionClient;
  }

  @Transactional(readOnly = true)
  public CustomerWalletResponse get(UUID userId) {
    UUID customerId = customers.findByUserId(userId)
        .orElseThrow(CustomerNotFoundException::new)
        .getCustomerId();
    CustomerWallet wallet = wallets.findByCustomerIdAndWalletRole(customerId, "PRIMARY")
        .orElseThrow(CustomerWalletNotFoundException::new);
    return CustomerWalletResponse.from(wallet, transactionClient.getWallet(wallet.getWalletId()));
  }
}
