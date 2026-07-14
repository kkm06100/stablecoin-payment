package stablecointransaction.external.exception;

import stablecointransaction.exception.ApplicationException;

public class StablecoinTransactionRemoteException extends ApplicationException {
  private final int status;
  private final String remoteCode;

  public StablecoinTransactionRemoteException(int status, Throwable cause) {
    this(status, null, cause);
  }

  public StablecoinTransactionRemoteException(int status, String remoteCode, Throwable cause) {
    super("stablecoin transaction request failed", cause);
    this.status = status;
    this.remoteCode = remoteCode;
  }

  public int getStatus() { return status; }
  public String getRemoteCode() { return remoteCode; }
}
