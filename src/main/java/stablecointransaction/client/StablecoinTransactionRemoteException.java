package stablecointransaction.client;

public class StablecoinTransactionRemoteException extends RuntimeException {
  private final int status;

  public StablecoinTransactionRemoteException(int status, String message, Throwable cause) {
    super(message, cause);
    this.status = status;
  }

  public int getStatus() { return status; }
}
