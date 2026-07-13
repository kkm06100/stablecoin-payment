package stablecointransaction.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMerchantRequest(
    @NotBlank @Size(max = 100) String merchant_name,
    @Size(max = 32) String business_number) {}
