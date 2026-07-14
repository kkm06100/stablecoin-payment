package stablecointransaction.userauth;

import stablecointransaction.userauth.service.LoginService;
import stablecointransaction.userauth.service.LogoutService;
import stablecointransaction.userauth.service.RefreshTokenService;
import stablecointransaction.userauth.service.SignupService;

import jakarta.validation.Valid;
import stablecointransaction.userauth.dto.AuthTokenResponse;
import stablecointransaction.userauth.dto.LoginRequest;
import stablecointransaction.userauth.dto.RefreshRequest;
import stablecointransaction.userauth.dto.SignupRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserAuthPaths.USER_AUTH_PREFIX)
public class UserAuthController {
  private final SignupService signupService;
  private final LoginService loginService;
  private final RefreshTokenService refreshService;
  private final LogoutService logoutService;

  public UserAuthController(SignupService signupService, LoginService loginService,
                            RefreshTokenService refreshService, LogoutService logoutService) {
    this.signupService = signupService;
    this.loginService = loginService;
    this.refreshService = refreshService;
    this.logoutService = logoutService;
  }

  @PostMapping("/signup")
  public AuthTokenResponse signup(@Valid @RequestBody SignupRequest request) {
    return signupService.signup(request.email(), request.password(), request.display_name());
  }

  @PostMapping("/login")
  public AuthTokenResponse login(@Valid @RequestBody LoginRequest request) {
    return loginService.login(request.email(), request.password());
  }

  @PostMapping("/refresh")
  public AuthTokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
    return refreshService.refresh(request.refresh_token());
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
    logoutService.logout(request.refresh_token());
    return ResponseEntity.noContent().build();
  }
}
