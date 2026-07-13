package stablecointransaction.payment;

public class PaymentNotFoundException extends RuntimeException {
  public PaymentNotFoundException(String message) { super(message); }
}
