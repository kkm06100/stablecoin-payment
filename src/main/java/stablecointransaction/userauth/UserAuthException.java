package stablecointransaction.userauth;

public class UserAuthException extends RuntimeException {
  public enum Code {
    EMAIL_ALREADY_REGISTERED,
    LOGIN_FAILED,
    USER_SUSPENDED,
    REFRESH_TOKEN_INVALID
  }

  private final Code code;

  public UserAuthException(Code code, String message) {
    super(message);
    this.code = code;
  }

  public Code getCode() { return code; }
}
