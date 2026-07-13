package stablecointransaction.support;

import org.springframework.jdbc.core.JdbcTemplate;

public final class TestDatabaseCleaner {

  private TestDatabaseCleaner() {
  }

  public static void cleanApplicationState(JdbcTemplate jdbc) {
    jdbc.execute("DELETE FROM payment.payment_qr_tokens");
    jdbc.execute("DELETE FROM payment.payments");
    jdbc.execute("DELETE FROM merchant.merchant_wallets");
    jdbc.execute("DELETE FROM merchant.merchant_members");
    jdbc.execute("DELETE FROM merchant.merchants");
    jdbc.execute("DELETE FROM identity.customer_wallets");
    jdbc.execute("DELETE FROM identity.customer_profiles");
    jdbc.execute("DELETE FROM identity.refresh_tokens");
    jdbc.execute("DELETE FROM identity.users");
    jdbc.execute("DELETE FROM ledger.postings");
    jdbc.execute("DELETE FROM ledger.journal_entries");
    jdbc.execute("DELETE FROM approver.withdrawal_decisions");
    jdbc.execute("DELETE FROM chain.outbox_messages");
    jdbc.execute("DELETE FROM chain.sweep_batch_deposits");
    jdbc.execute("DELETE FROM chain.sweep_batches");
    jdbc.execute("DELETE FROM chain.deposits");
    jdbc.execute("DELETE FROM chain.withdrawals");
    jdbc.execute("DELETE FROM chain.internal_transfers");
    jdbc.execute("DELETE FROM chain.wallet_token_accounts");
    jdbc.execute("DELETE FROM ledger.accounts");
    jdbc.execute("DELETE FROM auth.operator_public_keys WHERE operator_id LIKE 'op-%'");
    jdbc.execute("DELETE FROM wallet.wallets WHERE label NOT LIKE 'system-%'");
  }
}
