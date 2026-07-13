package stablecointransaction.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", schema = "identity")
public class RefreshToken {

  @Id
  @Column(name = "refresh_token_id")
  private UUID refreshTokenId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "token_family_id", nullable = false)
  private UUID tokenFamilyId;

  @Column(name = "token_hash", nullable = false, unique = true)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;

  @Column(name = "consumed_at")
  private OffsetDateTime consumedAt;

  @Column(name = "revoked_at")
  private OffsetDateTime revokedAt;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected RefreshToken() {}

  public RefreshToken(UUID refreshTokenId, UUID userId, UUID tokenFamilyId,
                      String tokenHash, OffsetDateTime expiresAt,
                      OffsetDateTime createdAt) {
    this.refreshTokenId = refreshTokenId;
    this.userId = userId;
    this.tokenFamilyId = tokenFamilyId;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
  }

  public UUID getRefreshTokenId() { return refreshTokenId; }
  public UUID getUserId() { return userId; }
  public UUID getTokenFamilyId() { return tokenFamilyId; }
  public String getTokenHash() { return tokenHash; }
  public OffsetDateTime getExpiresAt() { return expiresAt; }
  public OffsetDateTime getConsumedAt() { return consumedAt; }
  public OffsetDateTime getRevokedAt() { return revokedAt; }
  public OffsetDateTime getCreatedAt() { return createdAt; }

  public void consume(OffsetDateTime at) { this.consumedAt = at; }
  public void revoke(OffsetDateTime at) { this.revokedAt = at; }
}
