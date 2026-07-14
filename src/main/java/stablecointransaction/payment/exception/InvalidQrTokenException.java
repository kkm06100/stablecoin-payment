package stablecointransaction.payment.exception;

import stablecointransaction.exception.ApplicationException;

public class InvalidQrTokenException extends ApplicationException {
  public InvalidQrTokenException() { super("invalid QR token"); }
}
