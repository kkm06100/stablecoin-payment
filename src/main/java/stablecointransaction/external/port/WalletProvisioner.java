package stablecointransaction.external.port;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface WalletProvisioner {
  ProvisionedWallet create(String label);

  record ProvisionedWallet(@JsonProperty("wallet_id") UUID walletId, String label, String chain,
                           @JsonProperty("wallet_type") String walletType,
                           @JsonProperty("deposit_address") String depositAddress, String status) {}
}
