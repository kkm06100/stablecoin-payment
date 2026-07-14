package stablecointransaction.payment.component;

import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.userauth.exception.UserAuthException;
import stablecointransaction.payment.Payment;
import stablecointransaction.payment.PaymentReader;
import stablecointransaction.payment.qr.PaymentQrToken;
import stablecointransaction.payment.qr.QrTokenReader;
import stablecointransaction.user.CustomerProfile;
import stablecointransaction.user.CustomerProfileRepository;
import stablecointransaction.user.CustomerStatus;
import stablecointransaction.user.CustomerWallet;
import stablecointransaction.user.CustomerWalletRepository;
import stablecointransaction.user.CustomerWalletRoles;
import stablecointransaction.user.exception.CustomerNotFoundException;
import stablecointransaction.user.exception.CustomerWalletNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class PaymentConfirmationValidator {
  private final QrTokenReader qrTokens;
  private final PaymentReader payments;
  private final CustomerProfileRepository customers;
  private final CustomerWalletRepository customerWallets;

  public PaymentConfirmationValidator(QrTokenReader qrTokens,
                                      PaymentReader payments,
                                      CustomerProfileRepository customers,
                                      CustomerWalletRepository customerWallets) {
    this.qrTokens = qrTokens;
    this.payments = payments;
    this.customers = customers;
    this.customerWallets = customerWallets;
  }

  public ValidatedPayment validate(UUID userId, String rawToken, OffsetDateTime now) {
    PaymentQrToken qrToken = qrTokens.requireActive(rawToken, now);
    Payment payment = payments.requirePayable(qrToken.getPaymentId(), now);
    CustomerProfile customer = customers.findByUserId(userId)
        .orElseThrow(CustomerNotFoundException::new);
    if (customer.getStatus() != CustomerStatus.ACTIVE) {
      throw new UserAuthException(UserAuthException.Code.USER_SUSPENDED);
    }
    CustomerWallet wallet = customerWallets.findByCustomerIdAndWalletRole(
            customer.getCustomerId(), CustomerWalletRoles.PRIMARY)
        .orElseThrow(CustomerWalletNotFoundException::new);
    return new ValidatedPayment(qrToken, payment, customer.getCustomerId(), wallet);
  }

  public record ValidatedPayment(PaymentQrToken qrToken,
                                 Payment payment,
                                 UUID customerId,
                                 CustomerWallet wallet) {}
}
