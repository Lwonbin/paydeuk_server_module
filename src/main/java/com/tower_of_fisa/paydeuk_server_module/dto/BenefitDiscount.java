package com.tower_of_fisa.paydeuk_server_module.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
@Builder
public class BenefitDiscount {
    private Long benefitId;
    private Long spendingRangeId;
    private int discount;
    private boolean isDefaultCard;

    public static BenefitDiscount fromAdjusted(BenefitDiscount original, int adjustedAmount) {
        return BenefitDiscount.builder()
                .benefitId(original.getBenefitId())
                .spendingRangeId(original.getSpendingRangeId())
                .discount(adjustedAmount)
                .isDefaultCard(original.isDefaultCard())
                .build();
    }
}
