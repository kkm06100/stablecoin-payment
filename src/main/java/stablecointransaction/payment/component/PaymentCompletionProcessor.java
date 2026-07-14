package stablecointransaction.payment.component;

import java.time.OffsetDateTime;
import stablecointransaction.client.StablecoinTransactionClient;
import stablecointransaction.payment.PaymentRepository;
import stablecointransaction.payment.Payment;
import stablecointransaction.payment.exception.PaymentProcessingException;
import stablecointransaction.payment.exception.PaymentNotFoundException;
import stablecointransaction.payment.qr.PaymentQrTokenRepository;
import org.springframework.stereotype.Component;

@Component
public class PaymentCompletionProcessor {
  private final PaymentRepository payments;
  private final PaymentQrTokenRepository qrTokens;

  public PaymentCompletionProcessor(PaymentRepository payments,
                                    PaymentQrTokenRepository qrTokens) {
    this.payments = payments;
    this.qrTokens = qrTokens;
  }

  public Payment complete(StablecoinTransactionClient.RemoteTransfer transfer,
                         java.util.UUID paymentId, java.util.UUID qrTokenId,
                         OffsetDateTime now) {
    if (payments.markPaid(paymentId, transfer.transferId(), now) != 1) {
      throw new PaymentProcessingException();
    }
    if (qrTokens.markUsed(qrTokenId, now) != 1) {
      throw new PaymentProcessingException();
    }
    return payments.findById(paymentId).orElseThrow(PaymentNotFoundException::new);
  }
}
