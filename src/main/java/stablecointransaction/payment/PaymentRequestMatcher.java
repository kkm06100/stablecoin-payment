package stablecointransaction.payment;

import java.math.BigInteger;
import java.util.UUID;
import stablecointransaction.transfer.DuplicateRequestMismatchException;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestMatcher {
  void ensureSameRequest(Payment payment, UUID merchantWalletId,
                         String token, BigInteger amount) {
    boolean same = payment.getMerchantWalletId().equals(merchantWalletId)
        && payment.getToken().equals(token)
        && payment.getAmount().equals(amount);
    if (!same) {
      throw new DuplicateRequestMismatchException(
          "order_id " + payment.getOrderId() + " already used with different fields");
    }
  }
}
