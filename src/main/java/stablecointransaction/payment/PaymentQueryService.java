package stablecointransaction.payment;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import stablecointransaction.payment.exception.PaymentNotFoundException;
import stablecointransaction.merchant.MerchantAuthorization;
import stablecointransaction.user.CustomerProfileRepository;
import stablecointransaction.payment.dto.PaymentListResponse;
import stablecointransaction.payment.dto.PaymentResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentQueryService {
  private final PaymentRepository payments;
  private final MerchantAuthorization authorization;
  private final CustomerProfileRepository customers;

  public PaymentQueryService(PaymentRepository payments,
                             MerchantAuthorization authorization,
                             CustomerProfileRepository customers) {
    this.payments = payments;
    this.authorization = authorization;
    this.customers = customers;
  }

  @Transactional(readOnly = true)
  public PaymentResponse get(UUID userId, UUID merchantId, UUID paymentId) {
    authorization.requireMembership(userId, merchantId);
    Payment payment = payments.findById(paymentId)
        .filter(row -> row.getMerchantId().equals(merchantId))
        .orElseThrow(PaymentNotFoundException::new);
    return PaymentResponse.from(payment, null);
  }

  @Transactional(readOnly = true)
  public PaymentListResponse list(UUID userId, UUID merchantId,
                                  OffsetDateTime before, int limit) {
    authorization.requireMembership(userId, merchantId);
    int capped = Math.min(Math.max(limit, 1), 200);
    OffsetDateTime cursor = before == null ? OffsetDateTime.now() : before;
    List<Payment> rows = payments.findMerchantHistory(
        merchantId, cursor, PageRequest.of(0, capped));
    OffsetDateTime next = rows.isEmpty() ? null : rows.get(rows.size() - 1).getCreatedAt();
    return new PaymentListResponse(
        rows.stream().map(row -> PaymentResponse.from(row, null)).toList(), next);
  }

  @Transactional(readOnly = true)
  public PaymentResponse getForCustomer(UUID userId, UUID paymentId) {
    UUID customerId = customers.findByUserId(userId)
        .orElseThrow(PaymentNotFoundException::new)
        .getCustomerId();
    Payment payment = payments.findById(paymentId)
        .filter(row -> customerId.equals(row.getCustomerId()))
        .orElseThrow(PaymentNotFoundException::new);
    return PaymentResponse.from(payment, null);
  }

  @Transactional(readOnly = true)
  public PaymentListResponse listForCustomer(UUID userId, OffsetDateTime before, int limit) {
    UUID customerId = customers.findByUserId(userId)
        .orElseThrow(PaymentNotFoundException::new)
        .getCustomerId();
    int capped = Math.min(Math.max(limit, 1), 200);
    OffsetDateTime cursor = before == null ? OffsetDateTime.now() : before;
    List<Payment> rows = payments.findCustomerHistory(
        customerId, cursor, PageRequest.of(0, capped));
    OffsetDateTime next = rows.isEmpty() ? null : rows.get(rows.size() - 1).getCreatedAt();
    return new PaymentListResponse(
        rows.stream().map(row -> PaymentResponse.from(row, null)).toList(), next);
  }
}
