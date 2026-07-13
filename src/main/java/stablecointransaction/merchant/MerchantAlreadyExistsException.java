package stablecointransaction.merchant;

public class MerchantAlreadyExistsException extends RuntimeException {
  public MerchantAlreadyExistsException(String message) {
    super(message);
  }
}
