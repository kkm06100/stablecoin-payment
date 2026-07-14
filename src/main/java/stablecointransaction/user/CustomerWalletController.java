package stablecointransaction.user;

import stablecointransaction.userauth.UserAuthPaths;
import stablecointransaction.userauth.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserAuthPaths.WALLET_PREFIX)
public class CustomerWalletController {
  private final CustomerWalletService service;

  public CustomerWalletController(CustomerWalletService service) {
    this.service = service;
  }

  @GetMapping
  public CustomerWalletResponse get(@AuthenticationPrincipal Jwt jwt) {
    return service.get(UserPrincipal.from(jwt).userId());
  }
}
