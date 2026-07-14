package stablecointransaction.exception;

public class InternalApplicationException extends ApplicationException {
  private static final String MESSAGE = "internal application error";

  public InternalApplicationException() { super(MESSAGE); }
  public InternalApplicationException(Throwable cause) { super(MESSAGE, cause); }
}
