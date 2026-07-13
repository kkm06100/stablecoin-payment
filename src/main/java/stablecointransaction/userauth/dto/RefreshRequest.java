package stablecointransaction.userauth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank String refresh_token) {}
