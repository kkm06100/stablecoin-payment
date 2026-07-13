package stablecointransaction.merchant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "merchant_members", schema = "merchant")
@IdClass(MerchantMemberId.class)
public class MerchantMember {
  @Id
  @Column(name = "merchant_id")
  private UUID merchantId;

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "member_role", nullable = false)
  private String memberRole;

  @Column(nullable = false)
  private String status;

  @Column(name = "joined_at", nullable = false)
  private OffsetDateTime joinedAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected MerchantMember() {}

  public MerchantMember(UUID merchantId, UUID userId, String memberRole,
                        String status, OffsetDateTime joinedAt) {
    this.merchantId = merchantId;
    this.userId = userId;
    this.memberRole = memberRole;
    this.status = status;
    this.joinedAt = joinedAt;
    this.updatedAt = joinedAt;
  }

  public UUID getMerchantId() { return merchantId; }
  public UUID getUserId() { return userId; }
  public String getMemberRole() { return memberRole; }
  public String getStatus() { return status; }
  public OffsetDateTime getJoinedAt() { return joinedAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
