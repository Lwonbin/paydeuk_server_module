package com.tower_of_fisa.paydeuk_server_module.dto;

import lombok.Getter;

@Getter
public class PaymentResponse {
    private String paymentMoment;
    private Long benefitId;
    private Double discountAmount;
}
