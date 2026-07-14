package stablecointransaction.payment.exception;

import stablecointransaction.exception.ApplicationException;

public class PaymentProcessingException extends ApplicationException {
  public PaymentProcessingException() { super("payment processing failed"); }
}
