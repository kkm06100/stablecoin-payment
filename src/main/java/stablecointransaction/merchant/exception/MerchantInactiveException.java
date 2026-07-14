package stablecointransaction.merchant.exception;

import stablecointransaction.exception.ApplicationException;

public class MerchantInactiveException extends ApplicationException {
  public MerchantInactiveException() { super("merchant is inactive"); }
}
