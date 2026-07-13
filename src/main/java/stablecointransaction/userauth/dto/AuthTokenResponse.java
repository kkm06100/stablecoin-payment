package stablecointransaction.userauth.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuthTokenResponse(
    UUID user_id,
    String access_token,
    String refresh_token,
    OffsetDateTime access_token_expires_at,
    OffsetDateTime refresh_token_expires_at) {}
