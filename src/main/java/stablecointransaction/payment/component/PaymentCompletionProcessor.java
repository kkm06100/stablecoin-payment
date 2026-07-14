package stablecointransaction.payment.component;

import java.time.OffsetDateTime;
import java.util.Objects;
import stablecointransaction.external.port.TransferGateway;
import stablecointransaction.payment.PaymentRepository;
import stablecointransaction.payment.Payment;
import stablecointransaction.payment.PaymentStatuses;
import stablecointransaction.payment.exception.PaymentProcessingException;
import stablecointransaction.payment.exception.PaymentNotFoundException;
import stablecointransaction.payment.qr.PaymentQrTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentCompletionProcessor {
  private final PaymentRepository payments;
  private final PaymentQrTokenRepository qrTokens;

  public PaymentCompletionProcessor(PaymentRepository payments,
                                    PaymentQrTokenRepository qrTokens) {
    this.payments = payments;
    this.qrTokens = qrTokens;
  }

  @Transactional
  public Payment complete(TransferGateway.TransferResult transfer,
                         java.util.UUID paymentId, java.util.UUID qrTokenId,
                         OffsetDateTime now) {
    Payment current = payments.findById(paymentId).orElseThrow(PaymentProcessingException::new);
    validateTransfer(current, transfer);
    if (!PaymentStatuses.PAID.equals(current.getStatus())
        && payments.markPaid(paymentId, transfer.transferId(), now) != 1) {
      throw new PaymentProcessingException();
    }
    var token = qrTokens.findById(qrTokenId).orElseThrow(PaymentProcessingException::new);
    if (token.getUsedAt() == null && qrTokens.markUsed(qrTokenId, now) != 1) {
      throw new PaymentProcessingException();
    }
    return payments.findById(paymentId).orElseThrow(PaymentNotFoundException::new);
  }

  private void validateTransfer(Payment payment, TransferGateway.TransferResult transfer) {
    String reference = stablecointransaction.payment.PaymentConstants.TRANSFER_REFERENCE_PREFIX
        + payment.getPaymentId();
    boolean completed = "CONFIRMED".equals(transfer.status())
        || "COMPLETED".equals(transfer.status())
        || "PAID".equals(transfer.status());
    if (transfer.transferId() == null
        || !Objects.equals(payment.getCustomerWalletId(), transfer.sourceWalletId())
        || !Objects.equals(payment.getMerchantWalletId(), transfer.destinationWalletId())
        || !payment.getToken().equals(transfer.token())
        || !payment.getAmount().equals(transfer.amount())
        || !reference.equals(transfer.referenceId())
        || !completed) {
      throw new PaymentProcessingException();
    }
  }
}
