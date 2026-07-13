package stablecointransaction.userauth;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "user-auth")
public class UserAuthProperties {
  private String issuer;
  private String jwtSecret;
  private Duration accessTokenTtl;
  private Duration refreshTokenTtl;

  public String getIssuer() { return issuer; }
  public void setIssuer(String issuer) { this.issuer = issuer; }
  public String getJwtSecret() { return jwtSecret; }
  public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }
  public Duration getAccessTokenTtl() { return accessTokenTtl; }
  public void setAccessTokenTtl(Duration accessTokenTtl) { this.accessTokenTtl = accessTokenTtl; }
  public Duration getRefreshTokenTtl() { return refreshTokenTtl; }
  public void setRefreshTokenTtl(Duration refreshTokenTtl) { this.refreshTokenTtl = refreshTokenTtl; }
}
