package stablecointransaction.external.adapter;

import stablecointransaction.external.port.WalletProvisioner;
import org.springframework.stereotype.Component;

@Component
public class WalletProvisionerAdapter implements WalletProvisioner {
  private final StablecoinTransactionRestClient client;

  public WalletProvisionerAdapter(StablecoinTransactionRestClient client) {
    this.client = client;
  }

  @Override
  public ProvisionedWallet create(String label) {
    return client.create(label);
  }
}
