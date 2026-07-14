package stablecointransaction.userauth.service;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;
import stablecointransaction.external.StablecoinTransactionClientProperties;
import stablecointransaction.external.port.TokenAccountRegistrar;
import stablecointransaction.external.port.WalletProvisioner;
import stablecointransaction.user.CustomerProfile;
import stablecointransaction.user.CustomerProfileRepository;
import stablecointransaction.user.CustomerStatus;
import stablecointransaction.user.CustomerWallet;
import stablecointransaction.user.CustomerWalletRepository;
import stablecointransaction.user.CustomerWalletRoles;
import stablecointransaction.user.User;
import stablecointransaction.user.UserRepository;
import stablecointransaction.user.UserStatus;
import stablecointransaction.userauth.dto.AuthTokenResponse;
import stablecointransaction.userauth.exception.UserAuthException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupService {
  private final UserRepository users;
  private final CustomerProfileRepository customers;
  private final CustomerWalletRepository wallets;
  private final PasswordEncoder passwordEncoder;
  private final AuthTokenService tokens;
  private final WalletProvisioner walletProvisioner;
  private final TokenAccountRegistrar tokenAccounts;
  private final StablecoinTransactionClientProperties properties;

  public SignupService(UserRepository users, CustomerProfileRepository customers,
                       CustomerWalletRepository wallets, PasswordEncoder passwordEncoder,
                       AuthTokenService tokens, WalletProvisioner walletProvisioner,
                       TokenAccountRegistrar tokenAccounts,
                       StablecoinTransactionClientProperties properties) {
    this.users = users;
    this.customers = customers;
    this.wallets = wallets;
    this.passwordEncoder = passwordEncoder;
    this.tokens = tokens;
    this.walletProvisioner = walletProvisioner;
    this.tokenAccounts = tokenAccounts;
    this.properties = properties;
  }

  @Transactional
  public AuthTokenResponse signup(String email, String password, String displayName) {
    String normalized = email.trim().toLowerCase(Locale.ROOT);
    if (users.findByEmailIgnoreCase(normalized).isPresent()) {
      throw new UserAuthException(UserAuthException.Code.EMAIL_ALREADY_REGISTERED);
    }
    OffsetDateTime now = OffsetDateTime.now();
    User user = users.save(new User(UUID.randomUUID(), normalized, null,
        passwordEncoder.encode(password), UserStatus.ACTIVE, now));
    CustomerProfile customer = customers.save(new CustomerProfile(UUID.randomUUID(),
        user.getUserId(), displayName.trim(), CustomerStatus.ACTIVE, now));
    var wallet = walletProvisioner.create("customer-" + customer.getCustomerId());
    tokenAccounts.register(wallet.walletId(), properties.getUsdcTestMint());
    wallets.save(new CustomerWallet(customer.getCustomerId(), wallet.walletId(),
        CustomerWalletRoles.PRIMARY, now));
    return tokens.issue(user, UUID.randomUUID(), now);
  }
}
