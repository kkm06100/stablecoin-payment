package stablecointransaction.payment;

public class PaymentExpiredException extends RuntimeException {
  public PaymentExpiredException(String message) { super(message); }
}
