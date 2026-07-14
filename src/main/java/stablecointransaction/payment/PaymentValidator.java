package stablecointransaction.payment;


import java.math.BigInteger;
import stablecointransaction.payment.exception.InvalidPaymentRequestException;
import org.springframework.stereotype.Component;

@Component
public class PaymentValidator {
  public void validate(String orderId, String token, BigInteger amount) {
    if (orderId == null || orderId.isBlank()) {
      throw new InvalidPaymentRequestException();
    }
    if (token == null || token.isBlank()) {
      throw new InvalidPaymentRequestException();
    }
    if (amount == null || amount.signum() <= 0) {
      throw new InvalidPaymentRequestException();
    }
  }
}
