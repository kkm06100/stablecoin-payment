package stablecointransaction.payment.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record PaymentListResponse(
    List<PaymentResponse> payments,
    OffsetDateTime next_cursor) {}
