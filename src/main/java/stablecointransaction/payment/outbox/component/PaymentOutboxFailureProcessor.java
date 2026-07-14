package stablecointransaction.payment.outbox.component;

import java.time.Duration;
import java.time.OffsetDateTime;
import stablecointransaction.external.exception.StablecoinTransactionRemoteException;
import stablecointransaction.exception.InternalApplicationException;
import stablecointransaction.payment.outbox.PaymentOutbox;
import stablecointransaction.payment.outbox.PaymentOutboxRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentOutboxFailureProcessor {
  private static final int MAX_ATTEMPTS = 3;
  private final PaymentOutboxRepository outbox;

  public PaymentOutboxFailureProcessor(PaymentOutboxRepository outbox) {
    this.outbox = outbox;
  }

  @Transactional
  public void failed(PaymentOutbox event, Exception error, OffsetDateTime now) {
    if (retryable(error) && event.getAttemptCount() < MAX_ATTEMPTS) {
      long delaySeconds = 1L << Math.max(0, event.getAttemptCount() - 1);
      event.markFailed(error.getMessage(), now.plus(Duration.ofSeconds(delaySeconds)), now);
    } else {
      event.markFailed(error.getMessage(), now, now);
    }
    outbox.save(event);
  }

  private boolean retryable(Exception error) {
    if (error instanceof InternalApplicationException) return false;
    if (!(error instanceof StablecoinTransactionRemoteException remote)) return false;
    return remote.getStatus() == 429 || remote.getStatus() >= 500;
  }
}
