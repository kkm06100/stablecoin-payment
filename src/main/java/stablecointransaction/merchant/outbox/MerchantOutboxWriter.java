package stablecointransaction.merchant.outbox;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MerchantOutboxWriter {
  private final MerchantOutboxRepository outbox;

  public MerchantOutboxWriter(MerchantOutboxRepository outbox) {
    this.outbox = outbox;
  }

  public MerchantOutbox writeProvisioning(UUID merchantId, OffsetDateTime now) {
    return outbox.save(new MerchantOutbox(
        UUID.randomUUID(), merchantId, MerchantOutboxStatuses.PROVISION,
        "merchant_" + merchantId, now));
  }
}
