package stablecointransaction.payment;

import java.time.OffsetDateTime;
import stablecointransaction.merchant.Merchant;
import stablecointransaction.merchant.MerchantNotFoundException;
import stablecointransaction.merchant.MerchantRepository;
import stablecointransaction.payment.dto.PaymentQrResponse;
import stablecointransaction.payment.qr.PaymentQrService;
import stablecointransaction.payment.qr.PaymentQrToken;
import stablecointransaction.payment.qr.PaymentQrTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentQrLookup {
  private final PaymentQrTokenRepository qrTokens;
  private final PaymentQrService qrService;
  private final PaymentRepository payments;
  private final MerchantRepository merchants;

  public PaymentQrLookup(PaymentQrTokenRepository qrTokens,
                         PaymentQrService qrService,
                         PaymentRepository payments,
                         MerchantRepository merchants) {
    this.qrTokens = qrTokens;
    this.qrService = qrService;
    this.payments = payments;
    this.merchants = merchants;
  }

  @Transactional(readOnly = true)
  public PaymentQrResponse get(String rawToken) {
    OffsetDateTime now = OffsetDateTime.now();
    PaymentQrToken qrToken = requireActiveToken(rawToken, now);
    Payment payment = requirePayablePayment(qrToken.getPaymentId(), now);
    Merchant merchant = merchants.findById(payment.getMerchantId())
        .orElseThrow(() -> new MerchantNotFoundException(
            "merchant " + payment.getMerchantId()));
    return PaymentQrResponse.from(payment, merchant);
  }

  PaymentQrToken requireActiveToken(String rawToken, OffsetDateTime now) {
    PaymentQrToken token = qrTokens.findByTokenHash(qrService.hash(rawToken))
        .orElseThrow(() -> new InvalidQrTokenException("QR token not found"));
    if (token.getRevokedAt() != null || token.getUsedAt() != null) {
      throw new InvalidQrTokenException("QR token is no longer active");
    }
    if (!token.getExpiresAt().isAfter(now)) {
      throw new PaymentExpiredException("QR token expired");
    }
    return token;
  }

  Payment requirePayablePayment(java.util.UUID paymentId, OffsetDateTime now) {
    Payment payment = payments.findById(paymentId)
        .orElseThrow(() -> new PaymentNotFoundException("payment " + paymentId));
    if (!payment.getExpiresAt().isAfter(now)) {
      throw new PaymentExpiredException("payment expired: " + paymentId);
    }
    if (!PaymentStatuses.CREATED.equals(payment.getStatus())) {
      throw new PaymentAlreadyProcessedException(
          "payment is not payable: " + payment.getStatus());
    }
    return payment;
  }
}
