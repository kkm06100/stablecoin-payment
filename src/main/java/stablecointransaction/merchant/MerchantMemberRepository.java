package stablecointransaction.merchant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantMemberRepository
    extends JpaRepository<MerchantMember, MerchantMemberId> {
  Optional<MerchantMember> findByMerchantIdAndUserId(UUID merchantId, UUID userId);
  List<MerchantMember> findByUserIdAndStatus(UUID userId, String status);
}
