package stablecointransaction.payment.exception;

import stablecointransaction.exception.ApplicationException;

public class PaymentAlreadyProcessedException extends ApplicationException {
  public PaymentAlreadyProcessedException() {
    super("payment is already processed or not claimable");
  }
}
