package stablecointransaction.payment;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.merchant.MerchantAuthorization;
import stablecointransaction.merchant.MerchantWallet;
import stablecointransaction.merchant.MerchantWalletRepository;
import stablecointransaction.merchant.MerchantWalletRoles;
import stablecointransaction.merchant.MerchantWalletStatuses;
import stablecointransaction.merchant.MerchantInactiveException;
import stablecointransaction.payment.dto.PaymentResponse;
import stablecointransaction.payment.qr.PaymentQrService;
import stablecointransaction.userauth.UserAuthPaths;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentCreator {
  private final PaymentRepository payments;
  private final MerchantAuthorization merchantAuthorization;
  private final MerchantWalletRepository merchantWallets;
  private final PaymentValidator validator;
  private final PaymentRequestMatcher requestMatcher;
  private final PaymentQrService qrService;

  public PaymentCreator(PaymentRepository payments,
                        MerchantAuthorization merchantAuthorization,
                        MerchantWalletRepository merchantWallets,
                        PaymentValidator validator,
                        PaymentRequestMatcher requestMatcher,
                        PaymentQrService qrService) {
    this.payments = payments;
    this.merchantAuthorization = merchantAuthorization;
    this.merchantWallets = merchantWallets;
    this.validator = validator;
    this.requestMatcher = requestMatcher;
    this.qrService = qrService;
  }

  @Transactional
  public PaymentResponse create(UUID userId, UUID merchantId, String orderId,
                                String token, BigInteger amount, String description) {
    validator.validate(orderId, token, amount);
    merchantAuthorization.requirePaymentCreation(userId, merchantId);
    MerchantWallet wallet = merchantWallets.findByMerchantIdAndWalletRoleAndStatus(
            merchantId, MerchantWalletRoles.SETTLEMENT, MerchantWalletStatuses.ACTIVE)
        .orElseThrow(() -> new MerchantInactiveException(
            "active settlement wallet not found: " + merchantId));

    OffsetDateTime now = OffsetDateTime.now();
    UUID candidateId = UUID.randomUUID();
    OffsetDateTime expiresAt = now.plus(PaymentConstants.QR_TTL);
    payments.insertIfAbsent(candidateId, merchantId, wallet.getWalletId(), userId,
        orderId, token, amount, description, PaymentStatuses.CREATED, expiresAt, now);

    Payment payment = payments.findByMerchantIdAndOrderIdForUpdate(merchantId, orderId)
        .orElseThrow(() -> new IllegalStateException("payment insert did not produce a row"));
    requestMatcher.ensureSameRequest(payment, wallet.getWalletId(), token, amount);

    String qrPayload = null;
    if (PaymentStatuses.CREATED.equals(payment.getStatus())
        && payment.getExpiresAt().isAfter(now)) {
      PaymentQrService.IssuedQr qr = qrService.issueOrReuse(
          payment.getPaymentId(), payment.getExpiresAt(), now);
      qrPayload = UserAuthPaths.PAYMENT_QR_PREFIX + "/" + qr.rawToken();
    }
    return PaymentResponse.from(payment, qrPayload);
  }
}
