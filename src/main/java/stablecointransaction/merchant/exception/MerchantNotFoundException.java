package stablecointransaction.merchant.exception;

import stablecointransaction.exception.ApplicationException;

public class MerchantNotFoundException extends ApplicationException {
  public MerchantNotFoundException() { super("merchant not found"); }
}
