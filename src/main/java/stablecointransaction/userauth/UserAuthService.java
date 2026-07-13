package stablecointransaction.userauth;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;
import stablecointransaction.user.CustomerProfile;
import stablecointransaction.user.CustomerProfileRepository;
import stablecointransaction.user.CustomerStatus;
import stablecointransaction.user.CustomerWallet;
import stablecointransaction.user.CustomerWalletRepository;
import stablecointransaction.user.CustomerWalletRoles;
import stablecointransaction.user.RefreshToken;
import stablecointransaction.user.RefreshTokenRepository;
import stablecointransaction.user.User;
import stablecointransaction.user.UserRepository;
import stablecointransaction.user.UserStatus;
import stablecointransaction.userauth.dto.AuthTokenResponse;
import stablecointransaction.client.StablecoinTransactionClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAuthService {
  private final UserRepository users;
  private final CustomerProfileRepository customers;
  private final CustomerWalletRepository customerWallets;
  private final RefreshTokenRepository refreshTokens;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final RefreshTokenGenerator refreshTokenGenerator;
  private final UserAuthProperties properties;
  private final StablecoinTransactionClient transactionClient;

  public UserAuthService(UserRepository users,
                         CustomerProfileRepository customers,
                         CustomerWalletRepository customerWallets,
                         RefreshTokenRepository refreshTokens,
                         PasswordEncoder passwordEncoder,
                         JwtService jwtService,
                         RefreshTokenGenerator refreshTokenGenerator,
                         UserAuthProperties properties,
                         StablecoinTransactionClient transactionClient) {
    this.users = users;
    this.customers = customers;
    this.customerWallets = customerWallets;
    this.refreshTokens = refreshTokens;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.refreshTokenGenerator = refreshTokenGenerator;
    this.properties = properties;
    this.transactionClient = transactionClient;
  }

  @Transactional
  public AuthTokenResponse signup(String email, String password, String displayName) {
    String normalizedEmail = normalizeEmail(email);
    if (users.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
      throw new UserAuthException(UserAuthException.Code.EMAIL_ALREADY_REGISTERED,
          "email already registered");
    }

    OffsetDateTime now = now();
    User user = users.save(new User(UUID.randomUUID(), normalizedEmail, null,
        passwordEncoder.encode(password), UserStatus.ACTIVE, now));
    CustomerProfile customer = customers.save(new CustomerProfile(UUID.randomUUID(),
        user.getUserId(), displayName.trim(), CustomerStatus.ACTIVE, now));
    StablecoinTransactionClient.RemoteWallet wallet = transactionClient.createUserWallet(
        "customer-" + customer.getCustomerId());
    customerWallets.save(new CustomerWallet(customer.getCustomerId(), wallet.walletId(),
        CustomerWalletRoles.PRIMARY, now));
    return issueTokenPair(user, UUID.randomUUID(), now);
  }

  @Transactional
  public AuthTokenResponse login(String email, String password) {
    User user = users.findByEmailIgnoreCase(normalizeEmail(email))
        .orElseThrow(this::loginFailed);
    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new UserAuthException(UserAuthException.Code.USER_SUSPENDED,
          "user is not active");
    }
    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      throw loginFailed();
    }
    return issueTokenPair(user, UUID.randomUUID(), now());
  }

  @Transactional(noRollbackFor = UserAuthException.class)
  public AuthTokenResponse refresh(String rawRefreshToken) {
    OffsetDateTime now = now();
    RefreshToken current = refreshTokens.findByTokenHash(
            refreshTokenGenerator.hash(rawRefreshToken))
        .orElseThrow(this::invalidRefreshToken);

    if (current.getConsumedAt() != null) {
      refreshTokens.revokeFamily(current.getTokenFamilyId(), now);
      throw invalidRefreshToken();
    }
    if (current.getRevokedAt() != null || !current.getExpiresAt().isAfter(now)) {
      throw invalidRefreshToken();
    }

    User user = users.findById(current.getUserId())
        .orElseThrow(this::invalidRefreshToken);
    if (user.getStatus() != UserStatus.ACTIVE) {
      refreshTokens.revokeFamily(current.getTokenFamilyId(), now);
      throw new UserAuthException(UserAuthException.Code.USER_SUSPENDED,
          "user is not active");
    }

    current.consume(now);
    return issueTokenPair(user, current.getTokenFamilyId(), now);
  }

  @Transactional
  public void logout(String rawRefreshToken) {
    refreshTokens.findByTokenHash(refreshTokenGenerator.hash(rawRefreshToken))
        .ifPresent(token -> token.revoke(now()));
  }

  private AuthTokenResponse issueTokenPair(User user, UUID familyId, OffsetDateTime now) {
    JwtService.AccessToken accessToken = jwtService.issueAccessToken(
        user.getUserId(), now.toInstant());
    String rawRefreshToken = refreshTokenGenerator.generate();
    OffsetDateTime refreshExpiresAt = now.plus(properties.getRefreshTokenTtl());
    refreshTokens.save(new RefreshToken(UUID.randomUUID(), user.getUserId(), familyId,
        refreshTokenGenerator.hash(rawRefreshToken), refreshExpiresAt, now));
    return new AuthTokenResponse(user.getUserId(), accessToken.value(), rawRefreshToken,
        accessToken.expiresAt(), refreshExpiresAt);
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private OffsetDateTime now() {
    return OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
  }

  private UserAuthException loginFailed() {
    return new UserAuthException(UserAuthException.Code.LOGIN_FAILED,
        "email or password is invalid");
  }

  private UserAuthException invalidRefreshToken() {
    return new UserAuthException(UserAuthException.Code.REFRESH_TOKEN_INVALID,
        "refresh token is invalid");
  }
}
