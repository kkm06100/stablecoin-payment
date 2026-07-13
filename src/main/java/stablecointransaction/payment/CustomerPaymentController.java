package stablecointransaction.payment;

import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.payment.dto.PaymentListResponse;
import stablecointransaction.payment.dto.PaymentResponse;
import stablecointransaction.userauth.UserAuthPaths;
import stablecointransaction.userauth.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserAuthPaths.PAYMENTS_PREFIX)
public class CustomerPaymentController {
  private final PaymentQueryService queries;

  public CustomerPaymentController(PaymentQueryService queries) {
    this.queries = queries;
  }

  @GetMapping
  public PaymentListResponse list(@AuthenticationPrincipal Jwt jwt,
                                  @RequestParam(required = false) OffsetDateTime before,
                                  @RequestParam(defaultValue = "50") int limit) {
    return queries.listForCustomer(UserPrincipal.from(jwt).userId(), before, limit);
  }

  @GetMapping("/{paymentId}")
  public PaymentResponse get(@AuthenticationPrincipal Jwt jwt,
                             @PathVariable UUID paymentId) {
    return queries.getForCustomer(UserPrincipal.from(jwt).userId(), paymentId);
  }
}
