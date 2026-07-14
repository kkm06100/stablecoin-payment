package stablecointransaction.payment.exception;

import stablecointransaction.exception.ApplicationException;

public class PaymentNotFoundException extends ApplicationException {
  public PaymentNotFoundException() { super("payment not found"); }
}
