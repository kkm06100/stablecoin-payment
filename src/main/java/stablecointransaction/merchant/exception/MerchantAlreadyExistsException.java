package stablecointransaction.merchant.exception;

import stablecointransaction.exception.ApplicationException;

public class MerchantAlreadyExistsException extends ApplicationException {
  public MerchantAlreadyExistsException() { super("merchant already exists"); }
}
