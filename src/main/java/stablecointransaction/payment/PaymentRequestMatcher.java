package stablecointransaction.payment;


import java.math.BigInteger;
import java.util.UUID;
import stablecointransaction.payment.exception.PaymentRequestMismatchException;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestMatcher {
  public void ensureSameRequest(Payment payment, UUID merchantWalletId,
                                String token, BigInteger amount) {
    boolean same = payment.getMerchantWalletId().equals(merchantWalletId)
        && payment.getToken().equals(token)
        && payment.getAmount().equals(amount);
    if (!same) {
      throw new PaymentRequestMismatchException();
    }
  }
}
