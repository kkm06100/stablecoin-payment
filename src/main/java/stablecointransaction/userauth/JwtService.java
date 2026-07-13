package stablecointransaction.userauth;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
  private final JwtEncoder encoder;
  private final UserAuthProperties properties;

  public JwtService(JwtEncoder encoder, UserAuthProperties properties) {
    this.encoder = encoder;
    this.properties = properties;
  }

  public AccessToken issueAccessToken(UUID userId, Instant issuedAt) {
    Instant expiresAt = issuedAt.plus(properties.getAccessTokenTtl());
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(properties.getIssuer())
        .subject(userId.toString())
        .issuedAt(issuedAt)
        .expiresAt(expiresAt)
        .id(UUID.randomUUID().toString())
        .build();
    String token = encoder.encode(JwtEncoderParameters.from(header, claims))
        .getTokenValue();
    return new AccessToken(token, OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC));
  }

  public record AccessToken(String value, OffsetDateTime expiresAt) {}
}
