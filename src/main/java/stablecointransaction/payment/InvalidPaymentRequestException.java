package stablecointransaction.payment;

public class InvalidPaymentRequestException extends RuntimeException {
  public InvalidPaymentRequestException(String message) { super(message); }
}
