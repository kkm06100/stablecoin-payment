package stablecointransaction.userauth.exception;

import stablecointransaction.exception.ApplicationException;

public class UserAuthException extends ApplicationException {
  public enum Code {
    EMAIL_ALREADY_REGISTERED,
    LOGIN_FAILED,
    USER_SUSPENDED,
    REFRESH_TOKEN_INVALID
  }

  private final Code code;

  public UserAuthException(Code code) {
    super(messageFor(code));
    this.code = code;
  }

  private static String messageFor(Code code) {
    return switch (code) {
      case EMAIL_ALREADY_REGISTERED -> "email is already registered";
      case LOGIN_FAILED -> "login failed";
      case USER_SUSPENDED -> "user is suspended";
      case REFRESH_TOKEN_INVALID -> "refresh token is invalid";
    };
  }

  public Code getCode() { return code; }
}
