package stablecointransaction.payment.component;

import stablecointransaction.external.port.TransferGateway;
import stablecointransaction.payment.Payment;
import stablecointransaction.payment.PaymentConstants;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransferProcessor {
  private final TransferGateway transactionClient;

  public PaymentTransferProcessor(TransferGateway transactionClient) {
    this.transactionClient = transactionClient;
  }

  public TransferGateway.TransferResult transfer(Payment payment, java.util.UUID customerWalletId) {
    String referenceId = PaymentConstants.TRANSFER_REFERENCE_PREFIX + payment.getPaymentId();
    return transactionClient.create(customerWalletId, payment.getMerchantWalletId(),
        payment.getToken(), payment.getAmount(), referenceId, null);
  }
}
