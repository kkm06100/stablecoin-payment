package stablecointransaction.merchant.outbox.component;

import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.merchant.Merchant;
import stablecointransaction.merchant.outbox.MerchantOutbox;
import stablecointransaction.merchant.outbox.MerchantOutboxRepository;
import stablecointransaction.merchant.MerchantRepository;
import stablecointransaction.merchant.MerchantWallet;
import stablecointransaction.merchant.MerchantWalletRepository;
import stablecointransaction.merchant.MerchantWalletRoles;
import stablecointransaction.merchant.MerchantWalletStatuses;
import stablecointransaction.merchant.exception.MerchantNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MerchantOutboxResultProcessor {
  private final MerchantOutboxRepository outboxes;
  private final MerchantRepository merchants;
  private final MerchantWalletRepository wallets;

  public MerchantOutboxResultProcessor(MerchantOutboxRepository outboxes,
                                       MerchantRepository merchants,
                                       MerchantWalletRepository wallets) {
    this.outboxes = outboxes;
    this.merchants = merchants;
    this.wallets = wallets;
  }

  @Transactional
  public void succeeded(MerchantOutbox outbox, UUID walletId, OffsetDateTime now) {
    Merchant merchant = merchants.findById(outbox.getMerchantId())
        .orElseThrow(MerchantNotFoundException::new);
    wallets.save(new MerchantWallet(merchant.getMerchantId(), walletId,
        MerchantWalletRoles.SETTLEMENT, MerchantWalletStatuses.ACTIVE, now));
    merchant.activate(now);
    merchants.save(merchant);
    outbox.markSucceeded(walletId, now);
    outboxes.save(outbox);
  }
}
