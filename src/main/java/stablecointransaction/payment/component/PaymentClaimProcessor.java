package stablecointransaction.payment.component;

import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.payment.Payment;
import stablecointransaction.payment.PaymentRepository;
import stablecointransaction.payment.exception.PaymentAlreadyProcessedException;
import stablecointransaction.payment.exception.PaymentExpiredException;
import stablecointransaction.payment.exception.PaymentNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class PaymentClaimProcessor {
  private final PaymentRepository payments;

  public PaymentClaimProcessor(PaymentRepository payments) {
    this.payments = payments;
  }

  public void claim(Payment payment, UUID customerId, UUID walletId,
                    OffsetDateTime now) {
    int claimed = payments.claim(payment.getPaymentId(), customerId,
        walletId, now);
    if (claimed == 1) return;

    Payment current = payments.findById(payment.getPaymentId())
        .orElseThrow(PaymentNotFoundException::new);
    if (!current.getExpiresAt().isAfter(now)) {
      throw new PaymentExpiredException();
    }
    throw new PaymentAlreadyProcessedException();
  }
}
