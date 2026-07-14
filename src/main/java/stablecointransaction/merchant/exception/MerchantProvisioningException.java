package stablecointransaction.merchant.exception;

import stablecointransaction.exception.ApplicationException;

public class MerchantProvisioningException extends ApplicationException {
  public MerchantProvisioningException() {
    super("merchant wallet provisioning failed");
  }

  public MerchantProvisioningException(Throwable cause) {
    super("merchant wallet provisioning failed", cause);
  }
}
