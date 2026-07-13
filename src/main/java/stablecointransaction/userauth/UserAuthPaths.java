package stablecointransaction.userauth;

public final class UserAuthPaths {
  public static final String USER_AUTH_PREFIX = "/v1/user-auth";
  public static final String MERCHANT_PREFIX = "/v1/merchants";
  public static final String PAYMENT_QR_PREFIX = "/v1/payment-qr";

  private UserAuthPaths() {}

  public static boolean isUserOwned(String path) {
    return belongsTo(path, USER_AUTH_PREFIX)
        || belongsTo(path, MERCHANT_PREFIX)
        || belongsTo(path, PAYMENT_QR_PREFIX);
  }

  private static boolean belongsTo(String path, String prefix) {
    return path.equals(prefix) || path.startsWith(prefix + "/");
  }
}
