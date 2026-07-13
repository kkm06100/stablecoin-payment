package stablecointransaction.payment;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistory, UUID> {
  List<PaymentStatusHistory> findByPaymentIdOrderByCreatedAtDesc(UUID paymentId);
}
