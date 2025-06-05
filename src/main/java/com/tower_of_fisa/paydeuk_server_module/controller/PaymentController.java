package com.tower_of_fisa.paydeuk_server_module.controller;

import com.tower_of_fisa.paydeuk_server_module.dto.ProcessPaymentRequest;
import com.tower_of_fisa.paydeuk_server_module.global.common.response.CommonResponse;
import com.tower_of_fisa.paydeuk_server_module.global.common.response.EmptyResponse;
import com.tower_of_fisa.paydeuk_server_module.global.common.response.SwaggerErrorResponseType;
import com.tower_of_fisa.paydeuk_server_module.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "PAYMENT API", description = "결제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/pay")
    @Operation(summary = "PAY_01 : 결제", description = "결제 프로세스")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "404",
                            description = "",
                            content = {@Content(schema = @Schema(implementation = SwaggerErrorResponseType.class))})
            })
    public CommonResponse<EmptyResponse> processPayment(@RequestBody ProcessPaymentRequest request) {
        paymentService.processPayment(request);
    return new CommonResponse<>(true, HttpStatus.OK, "message", new EmptyResponse());
    }
}
