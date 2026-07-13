package stablecointransaction.payment;

import java.math.BigInteger;
import org.springframework.stereotype.Component;

@Component
public class PaymentValidator {
  void validate(String orderId, String token, BigInteger amount) {
    if (orderId == null || orderId.isBlank()) {
      throw new InvalidPaymentRequestException("order_id required");
    }
    if (token == null || token.isBlank()) {
      throw new InvalidPaymentRequestException("token required");
    }
    if (amount == null || amount.signum() <= 0) {
      throw new InvalidPaymentRequestException("amount must be positive");
    }
  }
}
