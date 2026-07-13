package stablecointransaction.merchant;

public class MerchantAccessDeniedException extends RuntimeException {
  public MerchantAccessDeniedException(String message) { super(message); }
}
