package stablecointransaction.payment.service;

import stablecointransaction.payment.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.payment.component.PaymentClaimProcessor;
import stablecointransaction.payment.component.PaymentCompletionProcessor;
import stablecointransaction.payment.component.PaymentConfirmationValidator;
import stablecointransaction.payment.component.PaymentReplayReader;
import stablecointransaction.payment.outbox.component.PaymentOutboxWriter;
import stablecointransaction.payment.dto.PaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentConfirmationService {
  private final PaymentReplayReader replayReader;
  private final PaymentConfirmationValidator validator;
  private final PaymentClaimProcessor claimProcessor;
  private final PaymentOutboxWriter outboxWriter;

  public PaymentConfirmationService(PaymentReplayReader replayReader,
                                    PaymentConfirmationValidator validator,
                                    PaymentClaimProcessor claimProcessor,
                                    PaymentOutboxWriter outboxWriter) {
    this.replayReader = replayReader;
    this.validator = validator;
    this.claimProcessor = claimProcessor;
    this.outboxWriter = outboxWriter;
  }

  @Transactional
  public PaymentResponse confirm(UUID userId, String rawToken) {
    PaymentResponse replay = replayReader.findCompleted(rawToken)
        .map(payment -> PaymentResponse.from(payment, null))
        .orElse(null);
    if (replay != null) return replay;

    OffsetDateTime now = OffsetDateTime.now();
    PaymentConfirmationValidator.ValidatedPayment validated =
        validator.validate(userId, rawToken, now);
    claimProcessor.claim(validated.payment(), validated.customerId(), validated.walletId(), now);
    outboxWriter.writeTransfer(validated.payment(), validated.qrToken().getQrTokenId(), now);
    return PaymentResponse.withStatus(validated.payment(), PaymentStatuses.PROCESSING, null);
  }
}
