package stablecointransaction.userauth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserAuthPathsTest {

  @Test
  void recognizes_only_exact_user_owned_path_boundaries() {
    assertThat(UserAuthPaths.isUserOwned("/v1/user-auth/login")).isTrue();
    assertThat(UserAuthPaths.isUserOwned("/v1/merchants/merchant-id/payments")).isTrue();
    assertThat(UserAuthPaths.isUserOwned("/v1/payment-qr/token")).isTrue();

    assertThat(UserAuthPaths.isUserOwned("/v1/wallets")).isFalse();
    assertThat(UserAuthPaths.isUserOwned("/v1/transfers")).isFalse();
    assertThat(UserAuthPaths.isUserOwned("/v1/merchants-evil/path")).isFalse();
    assertThat(UserAuthPaths.isUserOwned("/v1/payment-qr-evil/path")).isFalse();
  }
}
