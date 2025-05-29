package com.tower_of_fisa.paydeuk_server_module.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class RecommendResponse {
    private String cardName;
    private String imageUrl;
    private String cardNumber;
    private Boolean isDefaultCard;
    private int discountAmount;
}
