package stablecointransaction.payment;

import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentExpiryProcessor {
  private final PaymentRepository payments;

  public PaymentExpiryProcessor(PaymentRepository payments) {
    this.payments = payments;
  }

  @Transactional
  public void expireCreatedPayments(OffsetDateTime now) {
    payments.expireCreated(now);
  }
}
