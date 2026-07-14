package stablecointransaction.external.port;

import java.math.BigInteger;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface TransferGateway {
  TransferResult create(UUID sourceWalletId, UUID destinationWalletId, String token,
                        BigInteger amount, String referenceId, String memo);

  TransferResult getTransfer(UUID transferId);

  record TransferResult(@JsonProperty("transfer_id") UUID transferId,
                        @JsonProperty("src_wallet_id") UUID sourceWalletId,
                        @JsonProperty("dst_wallet_id") UUID destinationWalletId,
                        String token, BigInteger amount,
                        @JsonProperty("reference_id") String referenceId, String status) {}
}
