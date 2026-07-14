package stablecointransaction.payment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import stablecointransaction.payment.component.PaymentClaimProcessor;
import stablecointransaction.payment.exception.PaymentAlreadyProcessedException;
import stablecointransaction.payment.exception.PaymentExpiredException;
import stablecointransaction.payment.exception.PaymentNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentClaimProcessorTest {
  private static final String TEST_ORDER_ID = "order";
  private static final String TEST_TOKEN = "USDC-test";
  private static final BigInteger TEST_AMOUNT = BigInteger.ONE;
  private static final String TEST_DESCRIPTION = "description";

  @Mock PaymentRepository payments;

  @Test
  void claimsCreatedPayment() {
    OffsetDateTime now = OffsetDateTime.now();
    Payment payment = payment(now.plusMinutes(5));
    UUID customerId = UUID.randomUUID();
    UUID walletId = UUID.randomUUID();
    when(payments.claim(payment.getPaymentId(), customerId, walletId, now)).thenReturn(1);

    new PaymentClaimProcessor(payments).claim(payment, customerId, walletId, now);

    verify(payments).claim(payment.getPaymentId(), customerId, walletId, now);
  }

  @Test
  void throwsAlreadyProcessedWhenClaimWasLost() {
    OffsetDateTime now = OffsetDateTime.now();
    Payment payment = payment(now.plusMinutes(5));
    when(payments.claim(eq(payment.getPaymentId()), any(), any(), any())).thenReturn(0);
    when(payments.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

    assertThatThrownBy(() -> new PaymentClaimProcessor(payments).claim(
        payment, UUID.randomUUID(), UUID.randomUUID(), now))
        .isInstanceOf(PaymentAlreadyProcessedException.class);
  }

  @Test
  void throwsExpiredWhenClaimWasLostBecausePaymentExpired() {
    OffsetDateTime now = OffsetDateTime.now();
    Payment payment = payment(now.minusMinutes(1));
    when(payments.claim(eq(payment.getPaymentId()), any(), any(), eq(now))).thenReturn(0);
    when(payments.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

    assertThatThrownBy(() -> new PaymentClaimProcessor(payments).claim(
        payment, UUID.randomUUID(), UUID.randomUUID(), now))
        .isInstanceOf(PaymentExpiredException.class);
  }

  @Test
  void throwsNotFoundWhenPaymentDisappearedAfterLostClaim() {
    OffsetDateTime now = OffsetDateTime.now();
    Payment payment = payment(now.plusMinutes(5));
    when(payments.claim(eq(payment.getPaymentId()), any(), any(), eq(now))).thenReturn(0);
    when(payments.findById(payment.getPaymentId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> new PaymentClaimProcessor(payments).claim(
        payment, UUID.randomUUID(), UUID.randomUUID(), now))
        .isInstanceOf(PaymentNotFoundException.class);
  }

  private Payment payment(OffsetDateTime expiresAt) {
    OffsetDateTime createdAt = expiresAt.minusMinutes(5);
    return new Payment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null,
        TEST_ORDER_ID, TEST_TOKEN, TEST_AMOUNT, TEST_DESCRIPTION, PaymentStatuses.CREATED,
        expiresAt, createdAt);
  }
}
