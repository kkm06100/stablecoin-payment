package stablecointransaction.payment.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import stablecointransaction.merchant.MerchantAuthorization;
import stablecointransaction.payment.Payment;
import stablecointransaction.payment.PaymentRepository;
import stablecointransaction.payment.dto.PaymentListResponse;
import stablecointransaction.payment.dto.PaymentResponse;
import stablecointransaction.payment.exception.PaymentNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantPaymentQueryService {
  private final PaymentRepository payments;
  private final MerchantAuthorization authorization;

  public MerchantPaymentQueryService(PaymentRepository payments,
                                     MerchantAuthorization authorization) {
    this.payments = payments;
    this.authorization = authorization;
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
}
