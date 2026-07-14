package stablecointransaction.payment;

import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.payment.exception.*;
import org.springframework.stereotype.Component;

@Component
public class PaymentReader {
  private final PaymentRepository payments;

  public PaymentReader(PaymentRepository payments) {
    this.payments = payments;
  }

  public Payment requirePayable(UUID paymentId, OffsetDateTime now) {
    Payment payment = payments.findById(paymentId)
        .orElseThrow(PaymentNotFoundException::new);
    if (!payment.getExpiresAt().isAfter(now)) {
      throw new PaymentExpiredException();
    }
    if (!PaymentStatuses.CREATED.equals(payment.getStatus())) {
      throw new PaymentAlreadyProcessedException();
    }
    return payment;
  }
}
