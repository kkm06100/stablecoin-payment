package stablecointransaction.merchant.outbox;

public final class MerchantOutboxStatuses {
  public static final String PENDING = "PENDING";
  public static final String PROCESSING = "PROCESSING";
  public static final String SUCCEEDED = "SUCCEEDED";
  public static final String FAILED = "FAILED";
  public static final String PROVISION = "MERCHANT_PROVISION";

  private MerchantOutboxStatuses() {}
}
