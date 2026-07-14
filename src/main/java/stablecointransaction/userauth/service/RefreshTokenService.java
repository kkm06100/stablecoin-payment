package stablecointransaction.userauth.service;

import java.time.OffsetDateTime;
import stablecointransaction.user.RefreshToken;
import stablecointransaction.user.RefreshTokenRepository;
import stablecointransaction.user.UserRepository;
import stablecointransaction.user.UserStatus;
import stablecointransaction.userauth.RefreshTokenGenerator;
import stablecointransaction.userauth.dto.AuthTokenResponse;
import stablecointransaction.userauth.exception.UserAuthException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {
  private final RefreshTokenRepository refreshTokens;
  private final RefreshTokenGenerator generator;
  private final UserRepository users;
  private final AuthTokenService tokens;

  public RefreshTokenService(RefreshTokenRepository refreshTokens, RefreshTokenGenerator generator,
                             UserRepository users, AuthTokenService tokens) {
    this.refreshTokens = refreshTokens;
    this.generator = generator;
    this.users = users;
    this.tokens = tokens;
  }

  @Transactional(noRollbackFor = UserAuthException.class)
  public AuthTokenResponse refresh(String rawToken) {
    OffsetDateTime now = OffsetDateTime.now();
    RefreshToken current = refreshTokens.findByTokenHash(generator.hash(rawToken))
        .orElseThrow(this::invalid);
    if (current.getConsumedAt() != null) {
      refreshTokens.revokeFamily(current.getTokenFamilyId(), now);
      throw invalid();
    }
    if (current.getRevokedAt() != null || !current.getExpiresAt().isAfter(now)) throw invalid();
    var user = users.findById(current.getUserId()).orElseThrow(this::invalid);
    if (user.getStatus() != UserStatus.ACTIVE) {
      refreshTokens.revokeFamily(current.getTokenFamilyId(), now);
      throw new UserAuthException(UserAuthException.Code.USER_SUSPENDED);
    }
    current.consume(now);
    return tokens.issue(user, current.getTokenFamilyId(), now);
  }

  private UserAuthException invalid() {
    return new UserAuthException(UserAuthException.Code.REFRESH_TOKEN_INVALID);
  }
}
