package stablecointransaction.payment.component;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.payment.Payment;
import stablecointransaction.payment.PaymentRepository;
import stablecointransaction.payment.PaymentRequestMatcher;
import stablecointransaction.payment.PaymentStatuses;
import stablecointransaction.payment.exception.PaymentProcessingException;
import org.springframework.stereotype.Component;

@Component
public class PaymentPersistenceProcessor {
  private final PaymentRepository payments;
  private final PaymentRequestMatcher requestMatcher;

  public PaymentPersistenceProcessor(PaymentRepository payments,
                                      PaymentRequestMatcher requestMatcher) {
    this.payments = payments;
    this.requestMatcher = requestMatcher;
  }

  public Payment persist(UUID candidateId, UUID merchantId, UUID merchantWalletId,
                         UUID userId, String orderId, String token, BigInteger amount,
                         String description, OffsetDateTime expiresAt, OffsetDateTime now) {
    payments.insertIfAbsent(candidateId, merchantId, merchantWalletId, userId,
        orderId, token, amount, description, PaymentStatuses.CREATED, expiresAt, now);
    Payment payment = payments.findByMerchantIdAndOrderIdForUpdate(merchantId, orderId)
        .orElseThrow(PaymentProcessingException::new);
    requestMatcher.ensureSameRequest(payment, merchantWalletId, token, amount);
    return payment;
  }
}
