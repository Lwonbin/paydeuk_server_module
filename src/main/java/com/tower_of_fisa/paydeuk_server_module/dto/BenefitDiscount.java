package com.tower_of_fisa.paydeuk_server_module.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class BenefitDiscount {
    private Long benefitId;
    private Long spendingRangeId;
    private int discount;
    private boolean isDefaultCard;
}
