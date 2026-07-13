package stablecointransaction.payment;

import java.time.Duration;

public final class PaymentConstants {
  public static final Duration QR_TTL = Duration.ofMinutes(5);
  public static final String TRANSFER_REFERENCE_PREFIX = "payment_";

  private PaymentConstants() {}
}
