package com.tower_of_fisa.paydeuk_server_module.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(title = "카드 추천: 카드 추천 Request")
public class RecommendRequest {
    @NotBlank(message = "사용자 Id를 입력해주세요.")
    @Schema(description = "userId", example = "1")
    private Long userId;

    @NotBlank(message = "가맹점 Id를 입력해주세요.")
    @Schema(description = "MerchantId", example = "7")
    private Long merchantId;

    @NotBlank(message = "결제 금액을 입력해주세요.")
    @Schema(description = "userId", example = "10000")
    private int amount;
}
