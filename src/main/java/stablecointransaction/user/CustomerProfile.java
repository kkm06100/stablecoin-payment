package stablecointransaction.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer_profiles", schema = "identity")
public class CustomerProfile {

  @Id
  @Column(name = "customer_id")
  private UUID customerId;

  @Column(name = "user_id", nullable = false, unique = true)
  private UUID userId;

  @Column(name = "display_name", nullable = false)
  private String displayName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CustomerStatus status;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected CustomerProfile() {}

  public CustomerProfile(UUID customerId, UUID userId, String displayName,
                         CustomerStatus status, OffsetDateTime createdAt) {
    this.customerId = customerId;
    this.userId = userId;
    this.displayName = displayName;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = createdAt;
  }

  public UUID getCustomerId() { return customerId; }
  public UUID getUserId() { return userId; }
  public String getDisplayName() { return displayName; }
  public CustomerStatus getStatus() { return status; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
