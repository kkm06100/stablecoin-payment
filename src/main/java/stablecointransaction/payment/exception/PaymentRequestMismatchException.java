package stablecointransaction.payment.exception;

import stablecointransaction.exception.ApplicationException;

public class PaymentRequestMismatchException extends ApplicationException {
  public PaymentRequestMismatchException() {
    super("payment request does not match the existing payment");
  }
}
