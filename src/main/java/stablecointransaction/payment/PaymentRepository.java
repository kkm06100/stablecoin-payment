package stablecointransaction.payment;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
  Optional<Payment> findByMerchantIdAndOrderId(UUID merchantId, String orderId);

  @Query("select p from Payment p where p.merchantId = :merchantId "
       + "and p.createdAt < :before order by p.createdAt desc, p.paymentId desc")
  List<Payment> findMerchantHistory(@Param("merchantId") UUID merchantId,
                                    @Param("before") OffsetDateTime before,
                                    Pageable pageable);

  @Query("select p from Payment p where p.customerId = :customerId "
       + "and p.createdAt < :before order by p.createdAt desc, p.paymentId desc")
  List<Payment> findCustomerHistory(@Param("customerId") UUID customerId,
                                    @Param("before") OffsetDateTime before,
                                    Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Payment p where p.merchantId = :merchantId and p.orderId = :orderId")
  Optional<Payment> findByMerchantIdAndOrderIdForUpdate(
      @Param("merchantId") UUID merchantId, @Param("orderId") String orderId);

  @Modifying(flushAutomatically = true)
  @Query(value = "insert into payment.payments (payment_id, merchant_id, "
       + "merchant_wallet_id, created_by, order_id, token, amount, description, "
       + "status, expires_at, created_at, updated_at) values (:paymentId, "
       + ":merchantId, :merchantWalletId, :createdBy, :orderId, :token, :amount, "
       + ":description, :status, :expiresAt, :now, :now) "
       + "on conflict (merchant_id, order_id) do nothing", nativeQuery = true)
  int insertIfAbsent(@Param("paymentId") UUID paymentId,
                     @Param("merchantId") UUID merchantId,
                     @Param("merchantWalletId") UUID merchantWalletId,
                     @Param("createdBy") UUID createdBy,
                     @Param("orderId") String orderId,
                     @Param("token") String token,
                     @Param("amount") java.math.BigInteger amount,
                     @Param("description") String description,
                     @Param("status") String status,
                     @Param("expiresAt") OffsetDateTime expiresAt,
                     @Param("now") OffsetDateTime now);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update Payment p set p.status = '" + PaymentStatuses.PROCESSING + "', "
       + "p.customerId = :customerId, p.customerWalletId = :customerWalletId, "
       + "p.processingAt = :now, p.updatedAt = :now "
       + "where p.paymentId = :paymentId and p.status = '" + PaymentStatuses.CREATED + "' "
       + "and p.expiresAt > :now")
  int claim(@Param("paymentId") UUID paymentId,
            @Param("customerId") UUID customerId,
            @Param("customerWalletId") UUID customerWalletId,
            @Param("now") OffsetDateTime now);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update Payment p set p.status = '" + PaymentStatuses.PAID
       + "', p.transferId = :transferId, "
       + "p.paidAt = :now, p.updatedAt = :now "
       + "where p.paymentId = :paymentId and p.status = '"
       + PaymentStatuses.PROCESSING + "'")
  int markPaid(@Param("paymentId") UUID paymentId,
               @Param("transferId") UUID transferId,
               @Param("now") OffsetDateTime now);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update Payment p set p.status = '" + PaymentStatuses.FAILED
       + "', p.failureCode = :failureCode, p.updatedAt = :now "
       + "where p.paymentId = :paymentId and p.status = '"
       + PaymentStatuses.PROCESSING + "'")
  int markFailed(@Param("paymentId") UUID paymentId,
                 @Param("failureCode") String failureCode,
                 @Param("now") OffsetDateTime now);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update Payment p set p.status = '" + PaymentStatuses.EXPIRED
       + "', p.updatedAt = :now where p.status = '" + PaymentStatuses.CREATED
       + "' and p.expiresAt <= :now")
  int expireCreated(@Param("now") OffsetDateTime now);
}
