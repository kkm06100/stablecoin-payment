package stablecointransaction.payment.outbox;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutbox, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from PaymentOutbox o where "
      + "(o.status = 'PENDING' or (o.status = 'FAILED' and o.nextAttemptAt <= :now) "
      + "or (o.status = 'PROCESSING' and o.lockedAt < :staleBefore)) "
      + "order by o.createdAt asc")
  java.util.List<PaymentOutbox> findProcessable(@Param("now") OffsetDateTime now,
                                                @Param("staleBefore") OffsetDateTime staleBefore,
                                                Pageable pageable);

  Optional<PaymentOutbox> findByIdempotencyKey(String idempotencyKey);
}
