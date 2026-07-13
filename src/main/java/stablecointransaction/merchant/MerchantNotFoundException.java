package stablecointransaction.merchant;

public class MerchantNotFoundException extends RuntimeException {
  public MerchantNotFoundException(String message) { super(message); }
}
