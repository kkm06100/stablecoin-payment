package stablecointransaction.userauth.service;

import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.user.User;
import stablecointransaction.user.RefreshToken;
import stablecointransaction.user.RefreshTokenRepository;
import stablecointransaction.userauth.JwtService;
import stablecointransaction.userauth.RefreshTokenGenerator;
import stablecointransaction.userauth.UserAuthProperties;
import stablecointransaction.userauth.dto.AuthTokenResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenService {
  private final RefreshTokenRepository refreshTokens;
  private final JwtService jwtService;
  private final RefreshTokenGenerator refreshTokenGenerator;
  private final UserAuthProperties properties;

  public AuthTokenService(RefreshTokenRepository refreshTokens, JwtService jwtService,
                          RefreshTokenGenerator refreshTokenGenerator,
                          UserAuthProperties properties) {
    this.refreshTokens = refreshTokens;
    this.jwtService = jwtService;
    this.refreshTokenGenerator = refreshTokenGenerator;
    this.properties = properties;
  }

  public AuthTokenResponse issue(User user, UUID familyId, OffsetDateTime now) {
    JwtService.AccessToken accessToken = jwtService.issueAccessToken(
        user.getUserId(), now.toInstant());
    String rawRefreshToken = refreshTokenGenerator.generate();
    OffsetDateTime refreshExpiresAt = now.plus(properties.getRefreshTokenTtl());
    refreshTokens.save(new RefreshToken(UUID.randomUUID(), user.getUserId(), familyId,
        refreshTokenGenerator.hash(rawRefreshToken), refreshExpiresAt, now));
    return new AuthTokenResponse(user.getUserId(), accessToken.value(), rawRefreshToken,
        accessToken.expiresAt(), refreshExpiresAt);
  }
}
