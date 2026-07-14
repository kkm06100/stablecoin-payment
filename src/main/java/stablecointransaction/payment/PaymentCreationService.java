package stablecointransaction.payment;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.merchant.MerchantWallet;
import stablecointransaction.payment.component.PaymentCreationContextResolver;
import stablecointransaction.payment.component.PaymentPersistenceProcessor;
import stablecointransaction.payment.component.PaymentQrPayloadFactory;
import stablecointransaction.payment.dto.PaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentCreationService {
  private final PaymentValidator validator;
  private final PaymentCreationContextResolver contextResolver;
  private final PaymentPersistenceProcessor persistenceProcessor;
  private final PaymentQrPayloadFactory qrPayloadFactory;

  public PaymentCreationService(PaymentValidator validator,
                                PaymentCreationContextResolver contextResolver,
                                PaymentPersistenceProcessor persistenceProcessor,
                                PaymentQrPayloadFactory qrPayloadFactory) {
    this.validator = validator;
    this.contextResolver = contextResolver;
    this.persistenceProcessor = persistenceProcessor;
    this.qrPayloadFactory = qrPayloadFactory;
  }

  @Transactional
  public PaymentResponse create(UUID userId, UUID merchantId, String orderId,
                                String token, BigInteger amount, String description) {
    validator.validate(orderId, token, amount);
    MerchantWallet wallet = contextResolver.resolve(userId, merchantId);
    OffsetDateTime now = OffsetDateTime.now();
    Payment payment = persistenceProcessor.persist(
        UUID.randomUUID(), merchantId, wallet.getWalletId(), userId,
        orderId, token, amount, description, now.plus(PaymentConstants.QR_TTL), now);
    return PaymentResponse.from(payment, qrPayloadFactory.create(payment, now));
  }
}
