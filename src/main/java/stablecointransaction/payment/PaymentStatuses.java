package stablecointransaction.payment;

public final class PaymentStatuses {
  public static final String CREATED = "CREATED";
  public static final String PROCESSING = "PROCESSING";
  public static final String PAID = "PAID";
  public static final String FAILED = "FAILED";
  public static final String EXPIRED = "EXPIRED";
  public static final String CANCELLED = "CANCELLED";
  public static final String REFUND_PENDING = "REFUND_PENDING";
  public static final String REFUNDED = "REFUNDED";

  private PaymentStatuses() {}
}
