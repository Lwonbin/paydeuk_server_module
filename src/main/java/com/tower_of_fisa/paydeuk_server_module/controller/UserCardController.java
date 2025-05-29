package com.tower_of_fisa.paydeuk_server_module.controller;

import com.tower_of_fisa.paydeuk_server_module.dto.RecommendRequest;
import com.tower_of_fisa.paydeuk_server_module.global.common.response.CommonResponse;
import com.tower_of_fisa.paydeuk_server_module.global.common.response.SwaggerErrorResponseType;
import com.tower_of_fisa.paydeuk_server_module.dto.RecommendResponse;
import com.tower_of_fisa.paydeuk_server_module.service.UserCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "CARD API", description = "카드 추천 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/card")
public class UserCardController {

    private final UserCardService userCardService;

    @PostMapping("/recommend")
    @Operation(summary = "RECOMMAND_01 : 추천", description = "혜택이 가장 높은 카드를 추천한다.") // Swagger API 기능 설명
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "404",
                            description = "사용 가능한 혜택이 있는 카드가 없다.", // Swagger API : 응답 케이스 설명
                            content = {@Content(schema = @Schema(implementation = SwaggerErrorResponseType.class))})
            })
    public CommonResponse<List<RecommendResponse>> recommendCard(@RequestBody RecommendRequest request) {
        List<RecommendResponse> response = userCardService.recommendCard(request);

        if (response.isEmpty()) {
            return new CommonResponse<>(true, HttpStatus.OK, "추천 가능한 카드가 없습니다.", response);
        }

        boolean hasDiscount = response.stream().anyMatch(r -> r.getDiscountAmount() > 0);

        String message = hasDiscount
                ? "카드 추천에 성공했습니다." // 혜택 있는 카드
                : "적용 가능한 혜택은 없지만 대표카드를 추천합니다."; // fallback

        return new CommonResponse<>(true, HttpStatus.OK, message, response);
    }
}
