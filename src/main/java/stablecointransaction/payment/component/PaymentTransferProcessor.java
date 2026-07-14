package stablecointransaction.payment.component;

import stablecointransaction.client.StablecoinTransactionClient;
import stablecointransaction.payment.Payment;
import stablecointransaction.payment.PaymentConstants;
import stablecointransaction.user.CustomerWallet;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransferProcessor {
  private final StablecoinTransactionClient transactionClient;

  public PaymentTransferProcessor(StablecoinTransactionClient transactionClient) {
    this.transactionClient = transactionClient;
  }

  public StablecoinTransactionClient.RemoteTransfer transfer(Payment payment,
                                                              CustomerWallet wallet) {
    String referenceId = PaymentConstants.TRANSFER_REFERENCE_PREFIX + payment.getPaymentId();
    return transactionClient.createTransfer(wallet.getWalletId(), payment.getMerchantWalletId(),
        payment.getToken(), payment.getAmount(), referenceId, null);
  }
}
