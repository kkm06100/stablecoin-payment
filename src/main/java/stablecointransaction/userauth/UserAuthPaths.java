package stablecointransaction.userauth;

public final class UserAuthPaths {
  public static final String USER_AUTH_PREFIX = "/v1/user-auth";
  public static final String MERCHANT_PREFIX = "/v1/merchants";
  public static final String PAYMENT_QR_PREFIX = "/v1/payment-qr";
  public static final String PAYMENTS_PREFIX = "/v1/payments";
  public static final String WALLET_PREFIX = "/v1/me/wallet";

  private UserAuthPaths() {}

  public static boolean isUserOwned(String path) {
    return belongsTo(path, USER_AUTH_PREFIX)
        || belongsTo(path, MERCHANT_PREFIX)
        || belongsTo(path, PAYMENT_QR_PREFIX)
        || belongsTo(path, PAYMENTS_PREFIX)
        || belongsTo(path, WALLET_PREFIX);
  }

  private static boolean belongsTo(String path, String prefix) {
    return path.equals(prefix) || path.startsWith(prefix + "/");
  }
}
