package stablecointransaction.payment.exception;

import stablecointransaction.exception.ApplicationException;

public class InvalidPaymentRequestException extends ApplicationException {
  public InvalidPaymentRequestException() { super("invalid payment request"); }
}
