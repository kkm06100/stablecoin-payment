package stablecointransaction.payment;

import java.time.OffsetDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentExpiryScheduler {
  private final PaymentRepository payments;

  public PaymentExpiryScheduler(PaymentRepository payments) {
    this.payments = payments;
  }

  @Scheduled(fixedDelayString = "${payment.expiry-fixed-delay-ms:60000}")
  @Transactional
  public void expireCreatedPayments() {
    payments.expireCreated(OffsetDateTime.now());
  }
}
