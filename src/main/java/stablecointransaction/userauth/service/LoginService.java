package stablecointransaction.userauth.service;

import java.time.OffsetDateTime;
import java.util.Locale;
import stablecointransaction.user.User;
import stablecointransaction.user.UserRepository;
import stablecointransaction.user.UserStatus;
import stablecointransaction.userauth.dto.AuthTokenResponse;
import stablecointransaction.userauth.exception.UserAuthException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
  private final UserRepository users;
  private final PasswordEncoder passwordEncoder;
  private final AuthTokenService tokens;

  public LoginService(UserRepository users, PasswordEncoder passwordEncoder,
                      AuthTokenService tokens) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.tokens = tokens;
  }

  public AuthTokenResponse login(String email, String password) {
    User user = users.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
        .orElseThrow(() -> new UserAuthException(UserAuthException.Code.LOGIN_FAILED));
    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new UserAuthException(UserAuthException.Code.USER_SUSPENDED);
    }
    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new UserAuthException(UserAuthException.Code.LOGIN_FAILED);
    }
    return tokens.issue(user, java.util.UUID.randomUUID(), OffsetDateTime.now());
  }
}
