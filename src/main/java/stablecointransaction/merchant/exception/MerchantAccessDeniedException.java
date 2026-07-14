package stablecointransaction.merchant.exception;

import stablecointransaction.exception.ApplicationException;

public class MerchantAccessDeniedException extends ApplicationException {
  public MerchantAccessDeniedException() { super("merchant access denied"); }
}
