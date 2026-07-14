package stablecointransaction.userauth.service;

import stablecointransaction.user.RefreshTokenRepository;
import stablecointransaction.userauth.RefreshTokenGenerator;
import org.springframework.stereotype.Service;

@Service
public class LogoutService {
  private final RefreshTokenRepository refreshTokens;
  private final RefreshTokenGenerator generator;

  public LogoutService(RefreshTokenRepository refreshTokens, RefreshTokenGenerator generator) {
    this.refreshTokens = refreshTokens;
    this.generator = generator;
  }

  public void logout(String rawToken) {
    refreshTokens.findByTokenHash(generator.hash(rawToken))
        .ifPresent(token -> token.revoke(java.time.OffsetDateTime.now()));
  }
}
