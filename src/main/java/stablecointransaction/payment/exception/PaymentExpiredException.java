package stablecointransaction.payment.exception;

import stablecointransaction.exception.ApplicationException;

public class PaymentExpiredException extends ApplicationException {
  public PaymentExpiredException() { super("payment expired"); }
}
