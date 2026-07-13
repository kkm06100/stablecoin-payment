package stablecointransaction.payment.qr;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentQrTokenRepository extends JpaRepository<PaymentQrToken, UUID> {
  Optional<PaymentQrToken> findByTokenHash(String tokenHash);
  Optional<PaymentQrToken> findFirstByPaymentIdAndRevokedAtIsNullAndUsedAtIsNullOrderByCreatedAtDesc(
      UUID paymentId);
  List<PaymentQrToken> findByPaymentIdOrderByCreatedAtDesc(UUID paymentId);

  @Modifying
  @Query("update PaymentQrToken t set t.revokedAt = :now "
       + "where t.paymentId = :paymentId and t.revokedAt is null and t.usedAt is null")
  int revokeActive(@Param("paymentId") UUID paymentId,
                   @Param("now") java.time.OffsetDateTime now);

  @Modifying
  @Query("update PaymentQrToken t set t.usedAt = :now "
       + "where t.qrTokenId = :qrTokenId and t.usedAt is null and t.revokedAt is null")
  int markUsed(@Param("qrTokenId") UUID qrTokenId,
               @Param("now") java.time.OffsetDateTime now);
}
