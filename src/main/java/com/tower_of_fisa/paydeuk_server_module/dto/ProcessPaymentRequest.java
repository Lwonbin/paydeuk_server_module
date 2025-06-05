package com.tower_of_fisa.paydeuk_server_module.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(title = "결제: 결제 Request")
public class ProcessPaymentRequest {
    @NotBlank(message = "사용자 Id가 없습니다.")
    @Schema(description = "userId", example = "1")
    private Long userId;

    @NotBlank(message = "카드 Id가 없습니다.")
    @Schema(description = "cardId", example = "1")
    private Long cardId;

    @NotBlank(message = "혜택 금액이 없습니다.")
    @Schema(description = "Amount", example = "10000")
    private Double amount;

    @Schema(description = "Merchant Id", example = "1")
    private Long merchantId;

    private String productName;
}
