package stablecointransaction.payment.component;

import java.util.Optional;
import stablecointransaction.payment.Payment;
import stablecointransaction.payment.PaymentRepository;
import stablecointransaction.payment.PaymentStatuses;
import stablecointransaction.payment.qr.PaymentQrToken;
import stablecointransaction.payment.qr.QrTokenReader;
import org.springframework.stereotype.Component;

@Component
public class PaymentReplayReader {
  private final PaymentRepository payments;
  private final QrTokenReader qrTokens;

  public PaymentReplayReader(PaymentRepository payments, QrTokenReader qrTokens) {
    this.payments = payments;
    this.qrTokens = qrTokens;
  }

  public Optional<Payment> findCompleted(String rawToken) {
    PaymentQrToken token = qrTokens.findByRawToken(rawToken);
    return payments.findById(token.getPaymentId())
        .filter(payment -> token.getUsedAt() != null
            && PaymentStatuses.PAID.equals(payment.getStatus()));
  }
}
