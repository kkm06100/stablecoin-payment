package stablecointransaction.merchant.outbox.component;

import java.time.OffsetDateTime;
import stablecointransaction.external.exception.StablecoinTransactionRemoteException;
import stablecointransaction.exception.InternalApplicationException;
import stablecointransaction.merchant.outbox.MerchantOutbox;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MerchantOutboxFailureProcessor {
  private static final int MAX_ATTEMPTS = 3;
  private final stablecointransaction.merchant.outbox.MerchantOutboxRepository outboxes;

  public MerchantOutboxFailureProcessor(stablecointransaction.merchant.outbox.MerchantOutboxRepository outboxes) {
    this.outboxes = outboxes;
  }

  @Transactional
  public void failed(MerchantOutbox outbox, Exception error, OffsetDateTime now) {
    boolean retryable = error instanceof StablecoinTransactionRemoteException remote
        && (remote.getStatus() == 429 || remote.getStatus() >= 500)
        && !(error instanceof InternalApplicationException);
    OffsetDateTime next = retryable && outbox.getAttemptCount() < MAX_ATTEMPTS
        ? now.plusSeconds((long) Math.pow(2, outbox.getAttemptCount() - 1))
        : now.plusYears(100);
    outbox.markFailed(error.getMessage(), next, now);
    outboxes.save(outbox);
  }
}
