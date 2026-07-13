package stablecointransaction.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PaymentStatusHistoryTest {
  @Test
  void exposes_state_transition_audit_fields() {
    UUID historyId = UUID.randomUUID();
    UUID paymentId = UUID.randomUUID();
    OffsetDateTime createdAt = OffsetDateTime.now();

    var history = new PaymentStatusHistory(historyId, paymentId,
        PaymentStatuses.CREATED, PaymentStatuses.PROCESSING, "qr-confirm", createdAt);

    assertThat(history.getHistoryId()).isEqualTo(historyId);
    assertThat(history.getPaymentId()).isEqualTo(paymentId);
    assertThat(history.getFromStatus()).isEqualTo(PaymentStatuses.CREATED);
    assertThat(history.getToStatus()).isEqualTo(PaymentStatuses.PROCESSING);
    assertThat(history.getReason()).isEqualTo("qr-confirm");
    assertThat(PaymentStatuses.REFUND_PENDING).isEqualTo("REFUND_PENDING");
    assertThat(PaymentStatuses.REFUNDED).isEqualTo("REFUNDED");
  }
}
