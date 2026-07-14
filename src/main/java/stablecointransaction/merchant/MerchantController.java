package stablecointransaction.merchant;

import stablecointransaction.merchant.service.MerchantCreationService;
import stablecointransaction.merchant.service.MerchantQueryService;

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
  private final MerchantCreationService creator;
  private final MerchantQueryService queries;

  public MerchantController(MerchantCreationService creator, MerchantQueryService queries) {
    this.creator = creator;
    this.queries = queries;
  }

  @PostMapping
  public MerchantResponse create(@AuthenticationPrincipal Jwt jwt,
                                 @Valid @RequestBody CreateMerchantRequest request) {
    UUID userId = UserPrincipal.from(jwt).userId();
    return creator.create(userId, request.merchant_name(), request.business_number());
  }

  @GetMapping("/{merchantId}")
  public MerchantResponse get(@AuthenticationPrincipal Jwt jwt,
                              @PathVariable UUID merchantId) {
    return queries.get(UserPrincipal.from(jwt).userId(), merchantId);
  }
}
