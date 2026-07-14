package stablecointransaction.user.exception;

import stablecointransaction.exception.ApplicationException;

public class CustomerNotFoundException extends ApplicationException {
  public CustomerNotFoundException() { super("customer not found"); }
}
