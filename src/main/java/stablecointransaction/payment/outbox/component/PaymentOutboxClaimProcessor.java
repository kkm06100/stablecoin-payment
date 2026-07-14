package stablecointransaction.payment.outbox.component;

import java.time.OffsetDateTime;
import java.util.Optional;
import stablecointransaction.payment.outbox.PaymentOutbox;
import stablecointransaction.payment.outbox.PaymentOutboxRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentOutboxClaimProcessor {
  private final PaymentOutboxRepository outbox;

  public PaymentOutboxClaimProcessor(PaymentOutboxRepository outbox) {
    this.outbox = outbox;
  }

  @Transactional
  public Optional<PaymentOutbox> claimNext(OffsetDateTime now) {
    return outbox.findProcessable(now, now.minusMinutes(5), PageRequest.of(0, 1))
        .stream().findFirst().map(event -> {
          event.markProcessing(now);
          return outbox.save(event);
        });
  }
}
