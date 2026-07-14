package stablecointransaction.external.port;

import java.util.UUID;

public interface TokenAccountRegistrar {
  void register(UUID walletId, String mint);
}
