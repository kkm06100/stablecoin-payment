package stablecointransaction.external.adapter;

import java.math.BigInteger;
import java.util.UUID;
import stablecointransaction.external.port.TransferGateway;
import org.springframework.stereotype.Component;

@Component
public class TransferGatewayAdapter implements TransferGateway {
  private final StablecoinTransactionRestClient client;

  public TransferGatewayAdapter(StablecoinTransactionRestClient client) {
    this.client = client;
  }

  @Override
  public TransferResult create(UUID sourceWalletId, UUID destinationWalletId, String token,
                               BigInteger amount, String referenceId, String memo) {
    return client.create(sourceWalletId, destinationWalletId, token, amount, referenceId, memo);
  }

  @Override
  public TransferResult getTransfer(UUID transferId) {
    return client.getTransfer(transferId);
  }
}
