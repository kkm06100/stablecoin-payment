package stablecointransaction.client.exception;

import stablecointransaction.exception.ApplicationException;

public class StablecoinTransactionRemoteException extends ApplicationException {
  private final int status;

  public StablecoinTransactionRemoteException(int status, Throwable cause) {
    super("stablecoin transaction request failed", cause);
    this.status = status;
  }

  public int getStatus() { return status; }
}
