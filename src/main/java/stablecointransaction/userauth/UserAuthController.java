package stablecointransaction.userauth;

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
  private final UserAuthService service;

  public UserAuthController(UserAuthService service) {
    this.service = service;
  }

  @PostMapping("/signup")
  public AuthTokenResponse signup(@Valid @RequestBody SignupRequest request) {
    return service.signup(request.email(), request.password(), request.display_name());
  }

  @PostMapping("/login")
  public AuthTokenResponse login(@Valid @RequestBody LoginRequest request) {
    return service.login(request.email(), request.password());
  }

  @PostMapping("/refresh")
  public AuthTokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
    return service.refresh(request.refresh_token());
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
    service.logout(request.refresh_token());
    return ResponseEntity.noContent().build();
  }
}
