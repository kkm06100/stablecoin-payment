package stablecointransaction.payment;

import jakarta.validation.Valid;
import java.util.UUID;
import java.time.OffsetDateTime;
import stablecointransaction.payment.dto.CreatePaymentRequest;
import stablecointransaction.payment.dto.PaymentListResponse;
import stablecointransaction.payment.dto.PaymentResponse;
import stablecointransaction.userauth.UserAuthPaths;
import stablecointransaction.userauth.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping(UserAuthPaths.MERCHANT_PREFIX + "/{merchantId}/payments")
public class MerchantPaymentController {
  private final PaymentCreationService creator;
  private final PaymentQueryService queries;

  public MerchantPaymentController(PaymentCreationService creator, PaymentQueryService queries) {
    this.creator = creator;
    this.queries = queries;
  }

  @PostMapping
  public PaymentResponse create(@AuthenticationPrincipal Jwt jwt,
                                @PathVariable UUID merchantId,
                                @Valid @RequestBody CreatePaymentRequest request) {
    return creator.create(UserPrincipal.from(jwt).userId(), merchantId,
        request.order_id(), request.token(), request.amount(), request.description());
  }

  @GetMapping("/{paymentId}")
  public PaymentResponse get(@AuthenticationPrincipal Jwt jwt,
                             @PathVariable UUID merchantId,
                             @PathVariable UUID paymentId) {
    return queries.get(UserPrincipal.from(jwt).userId(), merchantId, paymentId);
  }

  @GetMapping
  public PaymentListResponse list(@AuthenticationPrincipal Jwt jwt,
                                  @PathVariable UUID merchantId,
                                  @RequestParam(required = false) OffsetDateTime before,
                                  @RequestParam(defaultValue = "50") int limit) {
    return queries.list(UserPrincipal.from(jwt).userId(), merchantId, before, limit);
  }
}
