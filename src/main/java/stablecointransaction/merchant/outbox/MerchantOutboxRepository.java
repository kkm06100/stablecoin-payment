package stablecointransaction.merchant.outbox;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MerchantOutboxRepository extends JpaRepository<MerchantOutbox, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from MerchantOutbox o where "
      + "(o.status = 'PENDING' or (o.status = 'FAILED' and o.nextAttemptAt <= :now) "
      + "or (o.status = 'PROCESSING' and o.lockedAt < :staleBefore)) "
      + "order by o.createdAt asc")
  List<MerchantOutbox> findProcessable(@Param("now") OffsetDateTime now,
                                       @Param("staleBefore") OffsetDateTime staleBefore,
                                       Pageable pageable);
}
