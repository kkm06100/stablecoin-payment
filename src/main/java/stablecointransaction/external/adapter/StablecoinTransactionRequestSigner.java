package stablecointransaction.external.adapter;

public interface StablecoinTransactionRequestSigner {
  SignedHeaders sign(String method, String pathAndQuery, byte[] body, String timestamp);

  record SignedHeaders(String operatorId, String signatureHex, String timestamp) {}
}
