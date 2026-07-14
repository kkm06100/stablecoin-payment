package stablecointransaction.payment.outbox.component;

import stablecointransaction.payment.component.PaymentCompletionProcessor;

import java.time.OffsetDateTime;
import stablecointransaction.external.port.TransferGateway;
import stablecointransaction.payment.Payment;
import stablecointransaction.payment.PaymentRepository;
import stablecointransaction.payment.outbox.PaymentOutbox;
import stablecointransaction.payment.outbox.PaymentOutboxRepository;
import stablecointransaction.payment.qr.PaymentQrTokenRepository;
import stablecointransaction.payment.exception.PaymentNotFoundException;
import stablecointransaction.payment.qr.PaymentQrToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentOutboxResultProcessor {
  private final PaymentOutboxRepository outbox;
  private final PaymentRepository payments;
  private final PaymentQrTokenRepository qrTokens;
  private final PaymentCompletionProcessor completion;

  public PaymentOutboxResultProcessor(PaymentOutboxRepository outbox,
                                      PaymentRepository payments,
                                      PaymentQrTokenRepository qrTokens,
                                      PaymentCompletionProcessor completion) {
    this.outbox = outbox;
    this.payments = payments;
    this.qrTokens = qrTokens;
    this.completion = completion;
  }

  @Transactional
  public void succeeded(PaymentOutbox event,
                        TransferGateway.TransferResult transfer,
                        OffsetDateTime now) {
    Payment payment = payments.findById(event.getPaymentId())
        .orElseThrow(PaymentNotFoundException::new);
    PaymentQrToken token = qrTokens.findByPaymentIdOrderByCreatedAtDesc(payment.getPaymentId())
        .stream().findFirst().orElseThrow(PaymentNotFoundException::new);
    completion.complete(transfer, payment.getPaymentId(), token.getQrTokenId(), now);
    event.markSucceeded(transfer.transferId(), now);
    outbox.save(event);
  }

}
