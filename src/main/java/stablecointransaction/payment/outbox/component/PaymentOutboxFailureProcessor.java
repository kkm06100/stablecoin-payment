package stablecointransaction.payment.outbox.component;

import java.time.Duration;
import java.time.OffsetDateTime;
import stablecointransaction.external.exception.StablecoinTransactionRemoteException;
import stablecointransaction.exception.InternalApplicationException;
import stablecointransaction.payment.outbox.PaymentOutbox;
import stablecointransaction.payment.outbox.PaymentOutboxRepository;
import stablecointransaction.payment.PaymentRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentOutboxFailureProcessor {
  private static final int MAX_ATTEMPTS = 3;
  private final PaymentOutboxRepository outbox;
  private final PaymentRepository payments;

  public PaymentOutboxFailureProcessor(PaymentOutboxRepository outbox, PaymentRepository payments) {
    this.outbox = outbox;
    this.payments = payments;
  }

  @Transactional
  public void failed(PaymentOutbox event, Exception error, OffsetDateTime now) {
    PaymentOutbox current = outbox.findById(event.getEventId()).orElse(event);
    if (retryable(error) && current.getAttemptCount() < MAX_ATTEMPTS) {
      long delaySeconds = 1L << Math.max(0, current.getAttemptCount() - 1);
      current.markFailed(error.getMessage(), now.plus(Duration.ofSeconds(delaySeconds)), now);
    } else {
      current.markDead(error.getMessage(), now);
      if (!retryable(error)) {
        payments.markFailed(current.getPaymentId(), failureCode(error), now);
      }
    }
    outbox.saveAndFlush(current);
  }

  private String failureCode(Exception error) {
    if (error instanceof StablecoinTransactionRemoteException remote) {
      return "REMOTE_" + remote.getStatus();
    }
    return "PAYMENT_PROCESSING_FAILED";
  }

  private boolean retryable(Exception error) {
    if (error instanceof InternalApplicationException) return false;
    if (!(error instanceof StablecoinTransactionRemoteException remote)) return false;
    return remote.getStatus() == 429 || remote.getStatus() >= 500;
  }
}
