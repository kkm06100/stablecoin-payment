package stablecointransaction.merchant;

import java.util.Set;

public final class MerchantRoles {
  public static final String OWNER = "OWNER";
  public static final String MANAGER = "MANAGER";
  public static final String CASHIER = "CASHIER";
  public static final String VIEWER = "VIEWER";
  public static final Set<String> PAYMENT_CREATORS = Set.of(OWNER, MANAGER, CASHIER);

  private MerchantRoles() {}
}
