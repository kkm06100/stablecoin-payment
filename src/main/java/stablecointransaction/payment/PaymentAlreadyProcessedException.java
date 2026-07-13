package stablecointransaction.payment;

public class PaymentAlreadyProcessedException extends RuntimeException {
  public PaymentAlreadyProcessedException(String message) { super(message); }
}
