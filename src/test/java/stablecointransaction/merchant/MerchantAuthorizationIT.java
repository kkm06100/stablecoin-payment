package stablecointransaction.merchant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.support.PostgresIntegrationTest;
import stablecointransaction.user.User;
import stablecointransaction.user.UserRepository;
import stablecointransaction.user.UserStatus;
import stablecointransaction.wallet.Wallet;
import stablecointransaction.wallet.WalletRepository;
import stablecointransaction.wallet.WalletType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

class MerchantAuthorizationIT extends PostgresIntegrationTest {

  @Autowired MerchantRepository merchants;
  @Autowired MerchantMemberRepository members;
  @Autowired MerchantWalletRepository merchantWallets;
  @Autowired MerchantAuthorization authorization;
  @Autowired UserRepository users;
  @Autowired WalletRepository wallets;
  @Autowired JdbcTemplate jdbc;

  @BeforeEach
  void setup() { cleanDatabase(jdbc); }

  @AfterEach
  void cleanup() { cleanDatabase(jdbc); }

  @Test
  void active_cashier_can_create_payment_for_own_merchant() {
    // given
    OffsetDateTime now = OffsetDateTime.now();
    User cashier = user("cashier", now);
    Merchant merchant = merchant("merchant-one", MerchantStatuses.ACTIVE, now);
    members.saveAndFlush(new MerchantMember(merchant.getMerchantId(), cashier.getUserId(),
        MerchantRoles.CASHIER, MerchantMemberStatuses.ACTIVE, now));

    // when
    Merchant authorized = authorization.requirePaymentCreation(
        cashier.getUserId(), merchant.getMerchantId());

    // then
    assertThat(authorized.getMerchantId()).isEqualTo(merchant.getMerchantId());
  }

  @Test
  void member_cannot_create_payment_for_another_merchant() {
    // given
    OffsetDateTime now = OffsetDateTime.now();
    User owner = user("owner", now);
    Merchant ownMerchant = merchant("merchant-own", MerchantStatuses.ACTIVE, now);
    Merchant otherMerchant = merchant("merchant-other", MerchantStatuses.ACTIVE, now);
    members.saveAndFlush(new MerchantMember(ownMerchant.getMerchantId(), owner.getUserId(),
        MerchantRoles.OWNER, MerchantMemberStatuses.ACTIVE, now));

    // when
    Runnable authorize = () -> authorization.requirePaymentCreation(
        owner.getUserId(), otherMerchant.getMerchantId());

    // then
    assertThatThrownBy(authorize::run)
        .isInstanceOf(MerchantAccessDeniedException.class);
  }

  @Test
  void viewer_and_suspended_member_cannot_create_payment() {
    // given
    OffsetDateTime now = OffsetDateTime.now();
    User viewer = user("viewer", now);
    User suspended = user("suspended", now);
    Merchant merchant = merchant("merchant-role", MerchantStatuses.ACTIVE, now);
    members.saveAndFlush(new MerchantMember(merchant.getMerchantId(), viewer.getUserId(),
        MerchantRoles.VIEWER, MerchantMemberStatuses.ACTIVE, now));
    members.saveAndFlush(new MerchantMember(merchant.getMerchantId(), suspended.getUserId(),
        MerchantRoles.CASHIER, MerchantMemberStatuses.SUSPENDED, now));

    // when
    Runnable viewerAuthorize = () -> authorization.requirePaymentCreation(
        viewer.getUserId(), merchant.getMerchantId());
    Runnable suspendedAuthorize = () -> authorization.requirePaymentCreation(
        suspended.getUserId(), merchant.getMerchantId());

    // then
    assertThatThrownBy(viewerAuthorize::run)
        .isInstanceOf(MerchantAccessDeniedException.class);
    assertThatThrownBy(suspendedAuthorize::run)
        .isInstanceOf(MerchantAccessDeniedException.class);
  }

  @Test
  void one_merchant_has_one_active_settlement_wallet() {
    // given
    OffsetDateTime now = OffsetDateTime.now();
    Merchant merchant = merchant("merchant-wallet", MerchantStatuses.ACTIVE, now);
    Wallet first = wallets.saveAndFlush(wallet("settlement-one"));
    Wallet second = wallets.saveAndFlush(wallet("settlement-two"));
    merchantWallets.saveAndFlush(new MerchantWallet(merchant.getMerchantId(),
        first.getWalletId(), MerchantWalletRoles.SETTLEMENT,
        MerchantWalletStatuses.ACTIVE, now));

    // when
    Runnable secondSettlement = () -> merchantWallets.saveAndFlush(new MerchantWallet(
        merchant.getMerchantId(), second.getWalletId(), MerchantWalletRoles.SETTLEMENT,
        MerchantWalletStatuses.ACTIVE, now));

    // then
    assertThatThrownBy(secondSettlement::run)
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  private User user(String name, OffsetDateTime now) {
    return users.saveAndFlush(new User(UUID.randomUUID(), name + "@example.com", null,
        "hash", UserStatus.ACTIVE, now));
  }

  private Merchant merchant(String name, String status, OffsetDateTime now) {
    return merchants.saveAndFlush(new Merchant(UUID.randomUUID(), name,
        "business-" + name, status, now));
  }

  private Wallet wallet(String label) {
    UUID id = UUID.randomUUID();
    return new Wallet(id, label, "solana", WalletType.USER,
        "address-" + id, "key-" + id);
  }
}
