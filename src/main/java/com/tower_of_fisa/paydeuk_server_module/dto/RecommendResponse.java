package com.tower_of_fisa.paydeuk_server_module.dto;

import com.tower_of_fisa.paydeuk_server_module.domain.entity.UserCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@Builder
public class RecommendResponse {
    private Long cardId;
    private String cardName;
    private String imageUrl;
    private String cardNumber;
    private Boolean isDefaultCard;
    private int discountAmount;

    public static RecommendResponse from(UserCard userCard, int discount) {
        return RecommendResponse.builder()
                .cardId(userCard.getCard().getId())
                .cardName(userCard.getCard().getName())
                .imageUrl(userCard.getCard().getImageUrl())
                .cardNumber(userCard.getCardNumber())
                .isDefaultCard(userCard.getIsDefaultCard())
                .discountAmount(discount)
                .build();
    }

    public static RecommendResponse fromDefault(UserCard userCard) {
        return RecommendResponse.builder()
                .cardId(userCard.getCard().getId())
                .cardName(userCard.getCard().getName())
                .imageUrl(userCard.getCard().getImageUrl())
                .cardNumber(userCard.getCardNumber())
                .isDefaultCard(true)
                .discountAmount(0)
                .build();
    }
}
