package stablecointransaction.payment.outbox;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentOutboxScheduler {
  private final PaymentOutboxProcessor processor;

  public PaymentOutboxScheduler(PaymentOutboxProcessor processor) {
    this.processor = processor;
  }

  @Scheduled(fixedDelayString = "${payment.outbox-fixed-delay-ms:1000}")
  public void processPaymentOutbox() {
    processor.processOne();
  }
}
