package stablecointransaction.payment;

import java.time.OffsetDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentExpiryScheduler {
  private final PaymentExpiryProcessor expiryProcessor;

  public PaymentExpiryScheduler(PaymentExpiryProcessor expiryProcessor) {
    this.expiryProcessor = expiryProcessor;
  }

  @Scheduled(fixedDelayString = "${payment.expiry-fixed-delay-ms:60000}")
  public void expireCreatedPayments() {
    expiryProcessor.expireCreatedPayments(OffsetDateTime.now());
  }
}
