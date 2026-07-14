package stablecointransaction.merchant.outbox.component;

import java.time.OffsetDateTime;
import stablecointransaction.merchant.outbox.MerchantOutbox;
import stablecointransaction.merchant.outbox.MerchantOutboxRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MerchantOutboxClaimProcessor {
  private final MerchantOutboxRepository outboxes;

  public MerchantOutboxClaimProcessor(MerchantOutboxRepository outboxes) {
    this.outboxes = outboxes;
  }

  @Transactional
  public MerchantOutbox claimNext(OffsetDateTime now) {
    return outboxes.findProcessable(now, now.minusMinutes(5))
        .stream().findFirst().map(outbox -> {
          outbox.markProcessing(now);
          return outbox;
        }).orElse(null);
  }
}
