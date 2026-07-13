package stablecointransaction.payment;

public class InvalidQrTokenException extends RuntimeException {
  public InvalidQrTokenException(String message) { super(message); }
}
