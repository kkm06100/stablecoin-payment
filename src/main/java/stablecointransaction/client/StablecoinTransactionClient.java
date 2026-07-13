package stablecointransaction.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import java.util.UUID;

public interface StablecoinTransactionClient {
  RemoteWallet createUserWallet(String label);

  RemoteTransfer createTransfer(UUID srcWalletId, UUID dstWalletId, String token,
                                BigInteger amount, String referenceId, String memo);

  RemoteTransfer getTransfer(UUID transferId);

  record RemoteWallet(@JsonProperty("wallet_id") UUID walletId, String status) {}

  record RemoteTransfer(@JsonProperty("transfer_id") UUID transferId,
                        @JsonProperty("src_wallet_id") UUID srcWalletId,
                        @JsonProperty("dst_wallet_id") UUID dstWalletId,
                        String token, BigInteger amount,
                        @JsonProperty("reference_id") String referenceId,
                        String status) {}
}
