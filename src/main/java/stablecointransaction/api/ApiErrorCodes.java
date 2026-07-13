package stablecointransaction.api;

public final class ApiErrorCodes {

  public static final String ACCOUNT_FROZEN = "ACCOUNT_FROZEN";
  public static final String ALREADY_IN_FLIGHT = "ALREADY_IN_FLIGHT";
  public static final String BAD_REQUEST = "BAD_REQUEST";
  public static final String BLOCKHASH_NOT_FOUND = "BLOCKHASH_NOT_FOUND";
  public static final String DUPLICATE_REQUEST_MISMATCH = "DUPLICATE_REQUEST_MISMATCH";
  public static final String EMAIL_ALREADY_REGISTERED = "EMAIL_ALREADY_REGISTERED";
  public static final String INSUFFICIENT_BALANCE = "INSUFFICIENT_BALANCE";
  public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
  public static final String INVALID_ADDRESS = "INVALID_ADDRESS";
  public static final String INVALID_REQUEST = "INVALID_REQUEST";
  public static final String LOGIN_FAILED = "LOGIN_FAILED";
  public static final String MERCHANT_ACCESS_DENIED = "MERCHANT_ACCESS_DENIED";
  public static final String MERCHANT_INACTIVE = "MERCHANT_INACTIVE";
  public static final String MERCHANT_NOT_FOUND = "MERCHANT_NOT_FOUND";
  public static final String PAYMENT_ALREADY_PROCESSED = "PAYMENT_ALREADY_PROCESSED";
  public static final String PAYMENT_EXPIRED = "PAYMENT_EXPIRED";
  public static final String PAYMENT_NOT_FOUND = "PAYMENT_NOT_FOUND";
  public static final String NOT_FOUND = "NOT_FOUND";
  public static final String POLICY_DENIED = "POLICY_DENIED";
  public static final String REFRESH_TOKEN_INVALID = "REFRESH_TOKEN_INVALID";
  public static final String QR_TOKEN_INVALID = "QR_TOKEN_INVALID";
  public static final String SELF_TRANSFER_NOT_ALLOWED = "SELF_TRANSFER_NOT_ALLOWED";
  public static final String SEND_FAILED = "SEND_FAILED";
  public static final String SWEEP_NOT_FOUND = "SWEEP_NOT_FOUND";
  public static final String UNSUPPORTED_TOKEN = "UNSUPPORTED_TOKEN";
  public static final String USER_SUSPENDED = "USER_SUSPENDED";
  public static final String TRANSFER_NOT_FOUND = "TRANSFER_NOT_FOUND";
  public static final String WALLET_NO_UNSWEPT = "WALLET_NO_UNSWEPT";
  public static final String WALLET_NO_DEPOSIT_ADDRESS = "WALLET_NO_DEPOSIT_ADDRESS";
  public static final String WALLET_NOT_FOUND = "WALLET_NOT_FOUND";
  public static final String WALLET_NOT_USER = "WALLET_NOT_USER";
  public static final String WITHDRAWAL_NOT_FOUND = "WITHDRAWAL_NOT_FOUND";

  private ApiErrorCodes() {}
}
