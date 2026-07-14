package stablecointransaction.payment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import stablecointransaction.payment.component.PaymentClaimProcessor;
import stablecointransaction.payment.exception.PaymentAlreadyProcessedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentClaimProcessorTest {
  @Mock PaymentRepository payments;

  @Test
  void throwsAlreadyProcessedWhenClaimWasLost() {
    OffsetDateTime now = OffsetDateTime.now();
    Payment payment = new Payment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null,
        "order", "USDC-test", BigInteger.ONE, "description", PaymentStatuses.CREATED,
        now.plusMinutes(5), now);
    when(payments.claim(eq(payment.getPaymentId()), any(), any(), any())).thenReturn(0);
    when(payments.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

    assertThatThrownBy(() -> new PaymentClaimProcessor(payments).claim(
        payment, UUID.randomUUID(), UUID.randomUUID(), now))
        .isInstanceOf(PaymentAlreadyProcessedException.class);
  }
}
