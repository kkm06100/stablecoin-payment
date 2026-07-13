package stablecointransaction.user;

import java.util.UUID;
import stablecointransaction.client.StablecoinTransactionClient;
import stablecointransaction.userauth.UserAuthPaths;
import stablecointransaction.userauth.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserAuthPaths.WALLET_PREFIX)
public class CustomerWalletController {
  private final CustomerProfileRepository customers;
  private final CustomerWalletRepository wallets;
  private final StablecoinTransactionClient transactionClient;

  public CustomerWalletController(CustomerProfileRepository customers,
                                  CustomerWalletRepository wallets,
                                  StablecoinTransactionClient transactionClient) {
    this.customers = customers;
    this.wallets = wallets;
    this.transactionClient = transactionClient;
  }

  @GetMapping
  public CustomerWalletResponse get(@AuthenticationPrincipal Jwt jwt) {
    UUID userId = UserPrincipal.from(jwt).userId();
    UUID customerId = customers.findByUserId(userId)
        .orElseThrow(() -> new IllegalStateException("customer profile not found"))
        .getCustomerId();
    CustomerWallet wallet = wallets.findByCustomerIdAndWalletRole(customerId, "PRIMARY")
        .orElseThrow(() -> new IllegalStateException("primary wallet not found"));
    return CustomerWalletResponse.from(wallet, transactionClient.getWallet(wallet.getWalletId()));
  }
}
