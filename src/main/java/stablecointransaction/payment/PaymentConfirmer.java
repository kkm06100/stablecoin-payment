package stablecointransaction.payment;

import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.payment.dto.PaymentResponse;
import stablecointransaction.payment.qr.PaymentQrToken;
import stablecointransaction.payment.qr.PaymentQrTokenRepository;
import stablecointransaction.transfer.InternalTransfer;
import stablecointransaction.transfer.InternalTransferService;
import stablecointransaction.user.CustomerProfile;
import stablecointransaction.user.CustomerProfileRepository;
import stablecointransaction.user.CustomerStatus;
import stablecointransaction.user.CustomerWallet;
import stablecointransaction.user.CustomerWalletRepository;
import stablecointransaction.user.CustomerWalletRoles;
import stablecointransaction.userauth.UserAuthException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentConfirmer {
  private final PaymentRepository payments;
  private final PaymentQrLookup qrLookup;
  private final PaymentQrTokenRepository qrTokens;
  private final CustomerProfileRepository customers;
  private final CustomerWalletRepository customerWallets;
  private final InternalTransferService internalTransfers;

  public PaymentConfirmer(PaymentRepository payments,
                          PaymentQrLookup qrLookup,
                          PaymentQrTokenRepository qrTokens,
                          CustomerProfileRepository customers,
                          CustomerWalletRepository customerWallets,
                          InternalTransferService internalTransfers) {
    this.payments = payments;
    this.qrLookup = qrLookup;
    this.qrTokens = qrTokens;
    this.customers = customers;
    this.customerWallets = customerWallets;
    this.internalTransfers = internalTransfers;
  }

  @Transactional
  public PaymentResponse confirm(UUID userId, String rawToken) {
    OffsetDateTime now = OffsetDateTime.now();
    PaymentQrToken qrToken = qrLookup.requireActiveToken(rawToken, now);
    Payment payment = qrLookup.requirePayablePayment(qrToken.getPaymentId(), now);
    CustomerProfile customer = customers.findByUserId(userId)
        .orElseThrow(() -> new UserAuthException(UserAuthException.Code.USER_SUSPENDED,
            "active customer profile not found"));
    if (customer.getStatus() != CustomerStatus.ACTIVE) {
      throw new UserAuthException(UserAuthException.Code.USER_SUSPENDED,
          "customer is not active");
    }
    CustomerWallet customerWallet = customerWallets.findByCustomerIdAndWalletRole(
            customer.getCustomerId(), CustomerWalletRoles.PRIMARY)
        .orElseThrow(() -> new InvalidPaymentRequestException(
            "customer primary wallet not found"));

    int claimed = payments.claim(payment.getPaymentId(), customer.getCustomerId(),
        customerWallet.getWalletId(), now);
    if (claimed != 1) {
      Payment current = payments.findById(payment.getPaymentId())
          .orElseThrow(() -> new PaymentNotFoundException(
              "payment " + payment.getPaymentId()));
      if (!current.getExpiresAt().isAfter(now)) {
        throw new PaymentExpiredException("payment expired: " + payment.getPaymentId());
      }
      throw new PaymentAlreadyProcessedException(
          "payment is not claimable: " + current.getStatus());
    }

    String referenceId = PaymentConstants.TRANSFER_REFERENCE_PREFIX + payment.getPaymentId();
    InternalTransfer transfer = internalTransfers.create(customerWallet.getWalletId(),
        payment.getMerchantWalletId(), payment.getToken(), payment.getAmount(),
        referenceId, null);
    if (payments.markPaid(payment.getPaymentId(), transfer.getTransferId(), now) != 1) {
      throw new IllegalStateException("claimed payment could not be marked paid");
    }
    if (qrTokens.markUsed(qrToken.getQrTokenId(), now) != 1) {
      throw new IllegalStateException("payment QR token could not be consumed");
    }
    Payment paid = payments.findById(payment.getPaymentId())
        .orElseThrow(() -> new PaymentNotFoundException("payment " + payment.getPaymentId()));
    return PaymentResponse.from(paid, null);
  }
}
