package stablecointransaction.merchant.outbox;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MerchantOutboxRepository extends JpaRepository<MerchantOutbox, UUID> {
  @Query(value = "select * from merchant.merchant_outbox o where "
      + "(o.status = 'PENDING' or (o.status = 'FAILED' and o.next_attempt_at <= :now) "
      + "or (o.status = 'PROCESSING' and o.locked_at < :staleBefore)) "
      + "order by o.created_at asc limit 1 for update skip locked", nativeQuery = true)
  List<MerchantOutbox> findProcessable(@Param("now") OffsetDateTime now,
                                       @Param("staleBefore") OffsetDateTime staleBefore);
}
