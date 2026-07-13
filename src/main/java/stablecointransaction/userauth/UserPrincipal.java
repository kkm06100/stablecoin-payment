package stablecointransaction.userauth;

import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

public record UserPrincipal(UUID userId) {
  public static UserPrincipal from(Jwt jwt) {
    return new UserPrincipal(UUID.fromString(jwt.getSubject()));
  }
}
