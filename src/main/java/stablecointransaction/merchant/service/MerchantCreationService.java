package stablecointransaction.merchant.service;

import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.merchant.*;
import stablecointransaction.merchant.dto.MerchantResponse;
import stablecointransaction.merchant.exception.*;
import stablecointransaction.merchant.outbox.MerchantOutboxWriter;
import stablecointransaction.user.User;
import stablecointransaction.user.UserRepository;
import stablecointransaction.user.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantCreationService {
  private final MerchantRepository merchants;
  private final MerchantMemberRepository members;
  private final UserRepository users;
  private final MerchantOutboxWriter outboxWriter;

  public MerchantCreationService(MerchantRepository merchants, MerchantMemberRepository members,
                                 UserRepository users, MerchantOutboxWriter outboxWriter) {
    this.merchants = merchants;
    this.members = members;
    this.users = users;
    this.outboxWriter = outboxWriter;
  }

  @Transactional
  public MerchantResponse create(UUID ownerUserId, String merchantName, String businessNumber) {
    User owner = users.findById(ownerUserId)
        .orElseThrow(MerchantAccessDeniedException::new);
    if (owner.getStatus() != UserStatus.ACTIVE) throw new MerchantAccessDeniedException();
    String normalized = normalizeBusinessNumber(businessNumber);
    if (normalized != null && merchants.findByBusinessNumber(normalized).isPresent()) {
      throw new MerchantAlreadyExistsException();
    }
    OffsetDateTime now = OffsetDateTime.now();
    UUID merchantId = UUID.randomUUID();
    Merchant merchant = merchants.save(new Merchant(merchantId, merchantName.trim(), normalized,
        MerchantStatuses.PENDING, now));
    members.save(new MerchantMember(merchantId, ownerUserId, MerchantRoles.OWNER,
        MerchantMemberStatuses.ACTIVE, now));
    outboxWriter.writeProvisioning(merchantId, now);
    return MerchantResponse.from(merchant, null);
  }

  private String normalizeBusinessNumber(String value) {
    if (value == null || value.isBlank()) return null;
    return value.replaceAll("[^0-9]", "");
  }
}
