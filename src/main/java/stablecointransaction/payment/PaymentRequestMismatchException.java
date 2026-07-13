package stablecointransaction.payment;

public class PaymentRequestMismatchException extends RuntimeException {
  public PaymentRequestMismatchException(String message) {
    super(message);
  }
}
