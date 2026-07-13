package stablecointransaction.user;

import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.client.StablecoinTransactionClient.RemoteWallet;

public record CustomerWalletResponse(UUID wallet_id, String label, String chain,
                                    String wallet_type, String deposit_address,
                                    String status, OffsetDateTime created_at) {
  public static CustomerWalletResponse from(CustomerWallet local, RemoteWallet remote) {
    return new CustomerWalletResponse(local.getWalletId(), remote.label(), remote.chain(),
        remote.walletType(), remote.depositAddress(), remote.status(), local.getCreatedAt());
  }
}
