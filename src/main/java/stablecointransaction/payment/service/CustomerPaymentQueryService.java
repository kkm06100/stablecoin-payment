package stablecointransaction.payment.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import stablecointransaction.payment.Payment;
import stablecointransaction.payment.PaymentRepository;
import stablecointransaction.payment.dto.PaymentListResponse;
import stablecointransaction.payment.dto.PaymentResponse;
import stablecointransaction.payment.exception.PaymentNotFoundException;
import stablecointransaction.user.CustomerProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerPaymentQueryService {
  private final PaymentRepository payments;
  private final CustomerProfileRepository customers;

  public CustomerPaymentQueryService(PaymentRepository payments,
                                     CustomerProfileRepository customers) {
    this.payments = payments;
    this.customers = customers;
  }

  @Transactional(readOnly = true)
  public PaymentResponse get(UUID userId, UUID paymentId) {
    return PaymentResponse.from(findCustomerPayment(userId, paymentId), null);
  }

  @Transactional(readOnly = true)
  public PaymentListResponse list(UUID userId, OffsetDateTime before, int limit) {
    UUID customerId = customerId(userId);
    int capped = Math.min(Math.max(limit, 1), 200);
    OffsetDateTime cursor = before == null ? OffsetDateTime.now() : before;
    List<Payment> rows = payments.findCustomerHistory(
        customerId, cursor, PageRequest.of(0, capped));
    OffsetDateTime next = rows.isEmpty() ? null : rows.get(rows.size() - 1).getCreatedAt();
    return new PaymentListResponse(
        rows.stream().map(row -> PaymentResponse.from(row, null)).toList(), next);
  }

  private Payment findCustomerPayment(UUID userId, UUID paymentId) {
    UUID customerId = customerId(userId);
    return payments.findById(paymentId)
        .filter(row -> customerId.equals(row.getCustomerId()))
        .orElseThrow(PaymentNotFoundException::new);
  }

  private UUID customerId(UUID userId) {
    return customers.findByUserId(userId)
        .orElseThrow(PaymentNotFoundException::new).getCustomerId();
  }
}
