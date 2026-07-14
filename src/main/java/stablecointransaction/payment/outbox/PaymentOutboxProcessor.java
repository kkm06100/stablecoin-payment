package stablecointransaction.payment.outbox;

import stablecointransaction.payment.Payment;
import stablecointransaction.payment.PaymentRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import stablecointransaction.external.port.TransferGateway;
import stablecointransaction.payment.outbox.component.PaymentOutboxClaimProcessor;
import stablecointransaction.payment.outbox.component.PaymentOutboxFailureProcessor;
import stablecointransaction.payment.outbox.component.PaymentOutboxResultProcessor;
import stablecointransaction.payment.component.PaymentTransferProcessor;
import stablecointransaction.payment.exception.PaymentProcessingException;
import stablecointransaction.payment.PaymentConstants;
import stablecointransaction.external.exception.StablecoinTransactionRemoteException;
import org.springframework.stereotype.Component;

@Component
public class PaymentOutboxProcessor {
  private final PaymentOutboxClaimProcessor claimProcessor;
  private final PaymentOutboxResultProcessor resultProcessor;
  private final PaymentOutboxFailureProcessor failureProcessor;
  private final PaymentRepository payments;
  private final PaymentTransferProcessor transferProcessor;

  public PaymentOutboxProcessor(PaymentOutboxClaimProcessor claimProcessor,
                                PaymentOutboxResultProcessor resultProcessor,
                                PaymentOutboxFailureProcessor failureProcessor,
                                PaymentRepository payments,
                                PaymentTransferProcessor transferProcessor) {
    this.claimProcessor = claimProcessor;
    this.resultProcessor = resultProcessor;
    this.failureProcessor = failureProcessor;
    this.payments = payments;
    this.transferProcessor = transferProcessor;
  }

  public void processOne() {
    OffsetDateTime now = OffsetDateTime.now();
    Optional<PaymentOutbox> claimed = claimProcessor.claimNext(now);
    if (claimed.isEmpty()) return;
    PaymentOutbox event = claimed.get();
    Payment payment = null;
    try {
      payment = payments.findById(event.getPaymentId())
          .orElseThrow(PaymentProcessingException::new);
      if (payment.getCustomerWalletId() == null) {
        throw new PaymentProcessingException();
      }
      TransferGateway.TransferResult transfer = transferProcessor.transfer(
          payment, payment.getCustomerWalletId());
      resultProcessor.succeeded(event, transfer, OffsetDateTime.now());
    } catch (Exception error) {
      if (error instanceof StablecoinTransactionRemoteException remote
          && (remote.getStatus() == 429 || remote.getStatus() >= 500)) {
        try {
          String reference = PaymentConstants.TRANSFER_REFERENCE_PREFIX + event.getPaymentId();
          var recovered = transferProcessor.findByReference(
              paymentWallet(payment), reference).stream().findFirst();
          if (recovered.isPresent()) {
            resultProcessor.succeeded(event, recovered.get(), OffsetDateTime.now());
            return;
          }
        } catch (Exception recoveryError) {
          error.addSuppressed(recoveryError);
        }
      }
      failureProcessor.failed(event, error, OffsetDateTime.now());
    }
  }

  private java.util.UUID paymentWallet(Payment payment) {
    if (payment.getCustomerWalletId() == null) throw new PaymentProcessingException();
    return payment.getCustomerWalletId();
  }
}
