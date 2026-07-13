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
@Table(name = "users", schema = "identity")
public class User {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Column
  private String email;

  @Column
  private String phone;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserStatus status;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected User() {}

  public User(UUID userId, String email, String phone, String passwordHash,
              UserStatus status, OffsetDateTime createdAt) {
    this.userId = userId;
    this.email = email;
    this.phone = phone;
    this.passwordHash = passwordHash;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = createdAt;
  }

  public UUID getUserId() { return userId; }
  public String getEmail() { return email; }
  public String getPhone() { return phone; }
  public String getPasswordHash() { return passwordHash; }
  public UserStatus getStatus() { return status; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
