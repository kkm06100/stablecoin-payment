package stablecointransaction.user.exception;

import stablecointransaction.exception.ApplicationException;

public class CustomerWalletNotFoundException extends ApplicationException {
  public CustomerWalletNotFoundException() { super("customer wallet not found"); }
}
