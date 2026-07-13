package stablecointransaction.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.support.PostgresIntegrationTest;
import stablecointransaction.wallet.Wallet;
import stablecointransaction.wallet.WalletRepository;
import stablecointransaction.wallet.WalletType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

class UserPersistenceIT extends PostgresIntegrationTest {

  @Autowired UserRepository users;
  @Autowired RefreshTokenRepository refreshTokens;
  @Autowired CustomerProfileRepository customers;
  @Autowired CustomerWalletRepository customerWallets;
  @Autowired WalletRepository wallets;
  @Autowired JdbcTemplate jdbc;

  @BeforeEach
  void setup() {
    cleanDatabase(jdbc);
  }

  @AfterEach
  void cleanup() {
    cleanDatabase(jdbc);
  }

  @Test
  void saves_user_profile_refresh_token_and_primary_wallet() {
    // given
    OffsetDateTime now = OffsetDateTime.now();
    User user = new User(UUID.randomUUID(), "payer@example.com", null,
        "{bcrypt}hash", UserStatus.ACTIVE, now);
    Wallet wallet = userWallet("identity-wallet");

    // when
    users.saveAndFlush(user);
    wallets.saveAndFlush(wallet);
    CustomerProfile customer = customers.saveAndFlush(new CustomerProfile(
        UUID.randomUUID(), user.getUserId(), "payer", CustomerStatus.ACTIVE, now));
    customerWallets.saveAndFlush(new CustomerWallet(customer.getCustomerId(),
        wallet.getWalletId(), CustomerWalletRoles.PRIMARY, now));
    RefreshToken refreshToken = refreshTokens.saveAndFlush(new RefreshToken(
        UUID.randomUUID(), user.getUserId(), UUID.randomUUID(), "token-hash",
        now.plusDays(30), now));

    // then
    assertThat(users.findByEmailIgnoreCase("PAYER@EXAMPLE.COM")).isPresent();
    assertThat(customers.findByUserId(user.getUserId())).contains(customer);
    assertThat(customerWallets.findByCustomerIdAndWalletRole(
        customer.getCustomerId(), CustomerWalletRoles.PRIMARY)).isPresent();
    assertThat(refreshTokens.findByTokenHash("token-hash")).contains(refreshToken);
  }

  @Test
  void normalized_email_is_unique() {
    // given
    OffsetDateTime now = OffsetDateTime.now();
    users.saveAndFlush(new User(UUID.randomUUID(), "payer@example.com", null,
        "hash-1", UserStatus.ACTIVE, now));

    // when
    Runnable duplicateInsert = () -> users.saveAndFlush(new User(UUID.randomUUID(),
        "PAYER@example.com", null, "hash-2", UserStatus.ACTIVE, now));

    // then
    assertThatThrownBy(duplicateInsert::run)
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void user_requires_email_or_phone() {
    // given
    User user = new User(UUID.randomUUID(), null, null, "hash",
        UserStatus.ACTIVE, OffsetDateTime.now());

    // when
    Runnable insert = () -> users.saveAndFlush(user);

    // then
    assertThatThrownBy(insert::run)
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void user_has_at_most_one_customer_profile() {
    // given
    OffsetDateTime now = OffsetDateTime.now();
    User user = users.saveAndFlush(new User(UUID.randomUUID(), null, "+821012345678",
        "hash", UserStatus.ACTIVE, now));
    customers.saveAndFlush(new CustomerProfile(UUID.randomUUID(), user.getUserId(),
        "first", CustomerStatus.ACTIVE, now));

    // when
    Runnable secondProfile = () -> customers.saveAndFlush(new CustomerProfile(
        UUID.randomUUID(), user.getUserId(), "second", CustomerStatus.ACTIVE, now));

    // then
    assertThatThrownBy(secondProfile::run)
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void wallet_belongs_to_one_customer_and_customer_has_one_primary_wallet() {
    // given
    OffsetDateTime now = OffsetDateTime.now();
    CustomerProfile first = customer("owner-one", now);
    CustomerProfile second = customer("owner-two", now);
    Wallet firstWallet = wallets.saveAndFlush(userWallet("customer-wallet-one"));
    Wallet secondWallet = wallets.saveAndFlush(userWallet("customer-wallet-two"));
    customerWallets.saveAndFlush(new CustomerWallet(first.getCustomerId(),
        firstWallet.getWalletId(), CustomerWalletRoles.PRIMARY, now));

    // when
    Runnable duplicateWalletOwner = () -> customerWallets.saveAndFlush(new CustomerWallet(
        second.getCustomerId(), firstWallet.getWalletId(), CustomerWalletRoles.PRIMARY, now));
    Runnable secondPrimary = () -> customerWallets.saveAndFlush(new CustomerWallet(
        first.getCustomerId(), secondWallet.getWalletId(), CustomerWalletRoles.PRIMARY, now));

    // then
    assertThatThrownBy(duplicateWalletOwner::run)
        .isInstanceOf(DataIntegrityViolationException.class);
    clearPersistenceContext();
    assertThatThrownBy(secondPrimary::run)
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  private CustomerProfile customer(String name, OffsetDateTime now) {
    User user = users.saveAndFlush(new User(UUID.randomUUID(), name + "@example.com", null,
        "hash", UserStatus.ACTIVE, now));
    return customers.saveAndFlush(new CustomerProfile(UUID.randomUUID(), user.getUserId(),
        name, CustomerStatus.ACTIVE, now));
  }

  private Wallet userWallet(String label) {
    UUID id = UUID.randomUUID();
    return new Wallet(id, label, "solana", WalletType.USER,
        "address-" + id, "key-" + id);
  }
}
