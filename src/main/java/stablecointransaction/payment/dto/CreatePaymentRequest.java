package stablecointransaction.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigInteger;

public record CreatePaymentRequest(
    @NotBlank @Size(max = 64) String order_id,
    @NotBlank String token,
    @NotNull @Positive BigInteger amount,
    @Size(max = 256) String description) {}
