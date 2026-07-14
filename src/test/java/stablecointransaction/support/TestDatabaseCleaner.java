package stablecointransaction.support;

import org.springframework.jdbc.core.JdbcTemplate;

public final class TestDatabaseCleaner {

  private TestDatabaseCleaner() {
  }

  public static void cleanApplicationState(JdbcTemplate jdbc) {
    jdbc.execute("DELETE FROM merchant.merchant_outbox");
    jdbc.execute("DELETE FROM payment.payment_outbox");
    jdbc.execute("DELETE FROM payment.payment_qr_tokens");
    jdbc.execute("DELETE FROM payment.payments");
    jdbc.execute("DELETE FROM merchant.merchant_wallets");
    jdbc.execute("DELETE FROM merchant.merchant_members");
    jdbc.execute("DELETE FROM merchant.merchants");
    jdbc.execute("DELETE FROM identity.customer_wallets");
    jdbc.execute("DELETE FROM identity.customer_profiles");
    jdbc.execute("DELETE FROM identity.refresh_tokens");
    jdbc.execute("DELETE FROM identity.users");
  }
}
