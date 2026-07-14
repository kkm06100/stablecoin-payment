package stablecointransaction.external.adapter;

import java.util.UUID;
import stablecointransaction.external.port.TokenAccountRegistrar;
import org.springframework.stereotype.Component;

@Component
public class TokenAccountRegistrarAdapter implements TokenAccountRegistrar {
  private final StablecoinTransactionRestClient client;

  public TokenAccountRegistrarAdapter(StablecoinTransactionRestClient client) {
    this.client = client;
  }

  @Override
  public void register(UUID walletId, String mint) {
    client.register(walletId, mint);
  }
}
