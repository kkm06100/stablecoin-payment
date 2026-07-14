package stablecointransaction.payment.outbox.component;

import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.payment.Payment;
import stablecointransaction.payment.outbox.PaymentOutbox;
import stablecointransaction.payment.outbox.PaymentOutboxRepository;
import stablecointransaction.payment.outbox.PaymentOutboxStatuses;
import org.springframework.stereotype.Component;

@Component
public class PaymentOutboxWriter {
  private final PaymentOutboxRepository outbox;

  public PaymentOutboxWriter(PaymentOutboxRepository outbox) {
    this.outbox = outbox;
  }

  public PaymentOutbox writeTransfer(Payment payment, OffsetDateTime now) {
    String idempotencyKey = "payment_" + payment.getPaymentId();
    return outbox.save(new PaymentOutbox(
        UUID.randomUUID(), payment.getPaymentId(), PaymentOutboxStatuses.PAYMENT_TRANSFER,
        idempotencyKey, "{\"payment_id\":\"" + payment.getPaymentId() + "\"}", now));
  }
}
