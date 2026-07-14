package stablecointransaction.merchant.outbox;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MerchantOutboxScheduler {
  private final MerchantOutboxProcessor processor;

  public MerchantOutboxScheduler(MerchantOutboxProcessor processor) {
    this.processor = processor;
  }

  @Scheduled(fixedDelayString = "${merchant.outbox-fixed-delay-ms:1000}")
  public void process() {
    processor.processOne();
  }
}
