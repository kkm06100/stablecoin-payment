package stablecointransaction.payment.outbox;

public final class PaymentOutboxStatuses {
  public static final String PENDING = "PENDING";
  public static final String PROCESSING = "PROCESSING";
  public static final String SUCCEEDED = "SUCCEEDED";
  public static final String FAILED = "FAILED";
  public static final String PAYMENT_TRANSFER = "PAYMENT_TRANSFER";

  private PaymentOutboxStatuses() {}
}
