package stablecointransaction.external.adapter;

import java.util.UUID;
import stablecointransaction.external.port.WalletReader;
import org.springframework.stereotype.Component;

@Component
public class WalletReaderAdapter implements WalletReader {
  private final StablecoinTransactionRestClient client;

  public WalletReaderAdapter(StablecoinTransactionRestClient client) {
    this.client = client;
  }

  @Override
  public WalletDetails get(UUID walletId) {
    return client.get(walletId);
  }
}
