package stablecointransaction.payment;

import java.util.UUID;
import stablecointransaction.payment.dto.PaymentQrResponse;
import stablecointransaction.payment.dto.PaymentResponse;
import stablecointransaction.userauth.UserAuthPaths;
import stablecointransaction.userauth.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserAuthPaths.PAYMENT_QR_PREFIX)
public class PaymentQrController {
  private final PaymentQrLookup lookup;
  private final PaymentConfirmer confirmer;

  public PaymentQrController(PaymentQrLookup lookup, PaymentConfirmer confirmer) {
    this.lookup = lookup;
    this.confirmer = confirmer;
  }

  @GetMapping("/{token}")
  public PaymentQrResponse get(@PathVariable String token) {
    return lookup.get(token);
  }

  @PostMapping("/{token}/confirm")
  public PaymentResponse confirm(@AuthenticationPrincipal Jwt jwt,
                                 @PathVariable String token) {
    UUID userId = UserPrincipal.from(jwt).userId();
    return confirmer.confirm(userId, token);
  }
}
