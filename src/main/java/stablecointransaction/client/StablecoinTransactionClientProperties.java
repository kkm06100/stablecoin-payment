package stablecointransaction.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "stablecoin-transaction")
public class StablecoinTransactionClientProperties {
  private String baseUrl;
  private String usdcTestMint;

  public String getBaseUrl() { return baseUrl; }
  public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
  public String getUsdcTestMint() { return usdcTestMint; }
  public void setUsdcTestMint(String usdcTestMint) { this.usdcTestMint = usdcTestMint; }
}
