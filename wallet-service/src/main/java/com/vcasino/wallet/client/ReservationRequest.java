package com.vcasino.wallet.client;

import com.vcasino.wallet.entity.enums.Applicant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReservationRequest {
    BigDecimal amount;
    Applicant applicant;
    Long aggregateId;
    ReservationType type;
}
