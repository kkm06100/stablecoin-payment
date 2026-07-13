package stablecointransaction.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {
  private String qrSecret;

  public String getQrSecret() { return qrSecret; }
  public void setQrSecret(String qrSecret) { this.qrSecret = qrSecret; }
}
