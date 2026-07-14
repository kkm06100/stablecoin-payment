package stablecointransaction.payment;

import java.time.OffsetDateTime;
import stablecointransaction.merchant.exception.MerchantNotFoundException;
import stablecointransaction.merchant.Merchant;
import stablecointransaction.merchant.MerchantRepository;
import stablecointransaction.payment.dto.PaymentQrResponse;
import stablecointransaction.payment.qr.PaymentQrToken;
import stablecointransaction.payment.qr.QrTokenReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentQrQueryService {
  private final QrTokenReader qrTokens;
  private final PaymentReader payments;
  private final MerchantRepository merchants;

  public PaymentQrQueryService(QrTokenReader qrTokens,
                               PaymentReader payments,
                               MerchantRepository merchants) {
    this.qrTokens = qrTokens;
    this.payments = payments;
    this.merchants = merchants;
  }

  @Transactional(readOnly = true)
  public PaymentQrResponse get(String rawToken) {
    OffsetDateTime now = OffsetDateTime.now();
    PaymentQrToken qrToken = qrTokens.requireActive(rawToken, now);
    Payment payment = payments.requirePayable(qrToken.getPaymentId(), now);
    Merchant merchant = merchants.findById(payment.getMerchantId())
        .orElseThrow(MerchantNotFoundException::new);
    return PaymentQrResponse.from(payment, merchant);
  }
}
