package stablecointransaction.merchant;

import jakarta.validation.Valid;
import java.util.UUID;
import stablecointransaction.merchant.dto.CreateMerchantRequest;
import stablecointransaction.merchant.dto.MerchantResponse;
import stablecointransaction.userauth.UserAuthPaths;
import stablecointransaction.userauth.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserAuthPaths.MERCHANT_PREFIX)
public class MerchantController {
  private final MerchantService service;

  public MerchantController(MerchantService service) {
    this.service = service;
  }

  @PostMapping
  public MerchantResponse create(@AuthenticationPrincipal Jwt jwt,
                                 @Valid @RequestBody CreateMerchantRequest request) {
    UUID userId = UserPrincipal.from(jwt).userId();
    return service.create(userId, request.merchant_name(), request.business_number());
  }

  @GetMapping("/{merchantId}")
  public MerchantResponse get(@AuthenticationPrincipal Jwt jwt,
                              @PathVariable UUID merchantId) {
    return service.get(UserPrincipal.from(jwt).userId(), merchantId);
  }
}
