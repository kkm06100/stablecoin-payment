package stablecointransaction.payment;

import stablecointransaction.payment.exception.PaymentNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import stablecointransaction.user.CustomerProfile;
import stablecointransaction.user.CustomerProfileRepository;
import stablecointransaction.user.CustomerStatus;

@ExtendWith(MockitoExtension.class)
class PaymentQueryServiceTest {
  @Mock PaymentRepository payments;
  @Mock CustomerProfileRepository customers;

  private stablecointransaction.payment.service.CustomerPaymentQueryService service;
  private UUID userId;
  private UUID customerId;
  private UUID paymentId;
  private Payment payment;

  @BeforeEach
  void setUp() {
    service = new stablecointransaction.payment.service.CustomerPaymentQueryService(payments, customers);
    userId = UUID.randomUUID();
    customerId = UUID.randomUUID();
    paymentId = UUID.randomUUID();
    OffsetDateTime now = OffsetDateTime.now();
    payment = new Payment(paymentId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
        "order-1", "USDC-test", BigInteger.TEN, "coffee", PaymentStatuses.PAID,
        now.plusMinutes(5), now);
  }

  @Test
  void lists_only_customer_payments_with_cursor() {
    when(customers.findByUserId(userId)).thenReturn(Optional.of(profile()));
    when(payments.findCustomerHistory(eq(customerId), any(), any(Pageable.class)))
        .thenReturn(List.of(payment));

    var result = service.list(userId, null, 50);

    assertThat(result.payments()).hasSize(1);
    assertThat(result.payments().get(0).payment_id()).isEqualTo(paymentId);
    assertThat(result.next_cursor()).isEqualTo(payment.getCreatedAt());
  }

  @Test
  void gets_payment_only_when_customer_owns_it() {
    paymentCustomer(customerId);
    when(customers.findByUserId(userId)).thenReturn(Optional.of(profile()));
    when(payments.findById(paymentId)).thenReturn(Optional.of(payment));

    assertThat(service.get(userId, paymentId).payment_id()).isEqualTo(paymentId);
  }

  @Test
  void rejects_payment_owned_by_another_customer() {
    paymentCustomer(UUID.randomUUID());
    when(customers.findByUserId(userId)).thenReturn(Optional.of(profile()));
    when(payments.findById(paymentId)).thenReturn(Optional.of(payment));

    assertThatThrownBy(() -> service.get(userId, paymentId))
        .isInstanceOf(PaymentNotFoundException.class);
  }

  @Test
  void rejects_user_without_customer_profile() {
    when(customers.findByUserId(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.list(userId, null, 50))
        .isInstanceOf(PaymentNotFoundException.class);
  }

  private CustomerProfile profile() {
    return new CustomerProfile(customerId, userId, "Customer", CustomerStatus.ACTIVE,
        OffsetDateTime.now());
  }

  private void paymentCustomer(UUID id) {
    try {
      var field = Payment.class.getDeclaredField("customerId");
      field.setAccessible(true);
      field.set(payment, id);
    } catch (ReflectiveOperationException ex) {
      throw new AssertionError(ex);
    }
  }
}
