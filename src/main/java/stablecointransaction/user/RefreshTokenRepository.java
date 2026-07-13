package stablecointransaction.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByTokenHash(String tokenHash);
  List<RefreshToken> findByTokenFamilyId(UUID tokenFamilyId);

  @Modifying
  @Query("update RefreshToken t set t.revokedAt = :at "
       + "where t.tokenFamilyId = :familyId and t.revokedAt is null")
  int revokeFamily(@Param("familyId") UUID familyId,
                   @Param("at") java.time.OffsetDateTime at);
}
