package stablecointransaction.merchant.outbox;

import stablecointransaction.merchant.Merchant;
import stablecointransaction.merchant.MerchantRepository;

import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.external.port.TokenAccountRegistrar;
import stablecointransaction.external.port.WalletProvisioner;
import stablecointransaction.external.StablecoinTransactionClientProperties;
import stablecointransaction.merchant.outbox.component.MerchantOutboxClaimProcessor;
import stablecointransaction.merchant.outbox.component.MerchantOutboxFailureProcessor;
import stablecointransaction.merchant.outbox.component.MerchantOutboxResultProcessor;
import stablecointransaction.merchant.outbox.MerchantOutboxRepository;
import stablecointransaction.merchant.exception.MerchantProvisioningException;
import org.springframework.stereotype.Component;

@Component
public class MerchantOutboxProcessor {
  private final MerchantOutboxClaimProcessor claimProcessor;
  private final MerchantOutboxResultProcessor resultProcessor;
  private final MerchantOutboxFailureProcessor failureProcessor;
  private final MerchantRepository merchants;
  private final WalletProvisioner walletProvisioner;
  private final TokenAccountRegistrar tokenAccounts;
  private final StablecoinTransactionClientProperties properties;
  private final MerchantOutboxRepository outboxes;

  public MerchantOutboxProcessor(MerchantOutboxClaimProcessor claimProcessor,
                                 MerchantOutboxResultProcessor resultProcessor,
                                 MerchantOutboxFailureProcessor failureProcessor,
                                 MerchantRepository merchants,
                                 WalletProvisioner walletProvisioner,
                                 TokenAccountRegistrar tokenAccounts,
                                 StablecoinTransactionClientProperties properties,
                                 MerchantOutboxRepository outboxes) {
    this.claimProcessor = claimProcessor;
    this.resultProcessor = resultProcessor;
    this.failureProcessor = failureProcessor;
    this.merchants = merchants;
    this.walletProvisioner = walletProvisioner;
    this.tokenAccounts = tokenAccounts;
    this.properties = properties;
    this.outboxes = outboxes;
  }

  public void processOne() {
    OffsetDateTime now = OffsetDateTime.now();
    MerchantOutbox outbox = claimProcessor.claimNext(now);
    if (outbox == null) return;
    try {
      Merchant merchant = merchants.findById(outbox.getMerchantId())
          .orElseThrow(() -> new MerchantProvisioningException());
      UUID walletId = outbox.getWalletId();
      if (walletId == null) {
        WalletProvisioner.ProvisionedWallet wallet = walletProvisioner.create(
            "merchant-" + merchant.getMerchantId());
        walletId = wallet.walletId();
        outbox.markWalletProvisioned(walletId, OffsetDateTime.now());
        outboxes.saveAndFlush(outbox);
      }
      tokenAccounts.register(walletId, properties.getUsdcTestMint());
      resultProcessor.succeeded(outbox, walletId, OffsetDateTime.now());
    } catch (Exception error) {
      failureProcessor.failed(outbox, error, OffsetDateTime.now());
    }
  }
}
