package com.tower_of_fisa.paydeuk_server_module.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tower_of_fisa.paydeuk_server_module.dto.PaymentResponse;
import com.tower_of_fisa.paydeuk_server_module.global.common.ErrorDefineCode;
import com.tower_of_fisa.paydeuk_server_module.global.config.exception.custom.exception.InvalidJsonFormatException400;
import com.tower_of_fisa.paydeuk_server_module.global.config.exception.custom.exception.NetworkException503;
import com.tower_of_fisa.paydeuk_server_module.global.config.exception.custom.exception.NoSuchElementFoundException404;
import com.tower_of_fisa.paydeuk_server_module.dto.CardConditionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardApiClient {

    @Value("${card.api.url}")
    private String cardApiUrl;

    private final RestTemplate restTemplate;

    public int getLastMonthSpending(String cardToken) {
        String url = cardApiUrl+"/record";
        log.info("url={}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // 요청 객체 설정
        String requestJson = "{ \"cardToken\": \"" + cardToken + "\" }";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class);

            log.info("response.getBody() = {}", response.getBody());
            return parseSpendingValue(response.getBody());

        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NoSuchElementFoundException404(ErrorDefineCode.CARD_NOT_FOUND);
            }
            throw new NetworkException503(ErrorDefineCode.UNCAUGHT);
        }
    }

    // 실제 응답에서 실적 값을 파싱하는 예시 (JSON 파싱)
    private int parseSpendingValue(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);
            // response.value 값 추출
            return root.path("response").path("value").asInt();
        } catch (Exception e) {
            throw new InvalidJsonFormatException400(ErrorDefineCode.INVALID_JSON_FORMAT);
        }
    }

    public List<CardConditionResponse> getBenefitConditions(String cardToken, List<Long> conditionIds) {
        String url = cardApiUrl + "/check/condition";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("cardToken", cardToken);
        body.put("conditionId", conditionIds);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        try{
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            return parseCardConditionResponse(response.getBody());
        }catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NoSuchElementFoundException404(ErrorDefineCode.CARD_NOT_FOUND);
            }
            throw new NetworkException503(ErrorDefineCode.UNCAUGHT);
        }

    }

    private List<CardConditionResponse> parseCardConditionResponse(String body) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(body);

            JsonNode responseArray = root.path("response");

            return objectMapper.readerForListOf(CardConditionResponse.class)
                    .readValue(responseArray);
        } catch (Exception e) {
            throw new InvalidJsonFormatException400(ErrorDefineCode.INVALID_JSON_FORMAT);
        }
    }

    public PaymentResponse processPayment(String cardToken, Double amount, Long merchantId) {
        String url = cardApiUrl + "/payment";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("cardToken", cardToken);
        body.put("amount", amount);
        body.put("merchantId", merchantId);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        try{
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
             return parsePaymentResponse(response.getBody());
        }catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NoSuchElementFoundException404(ErrorDefineCode.CARD_NOT_FOUND);
            }
            throw new NetworkException503(ErrorDefineCode.UNCAUGHT);
        }
    }

    private PaymentResponse parsePaymentResponse(String body) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(body);

            JsonNode responseArray =root.path("response");
            return objectMapper.readerFor(PaymentResponse.class)
                    .readValue(responseArray);
        } catch (Exception e) {
            throw new InvalidJsonFormatException400(ErrorDefineCode.INVALID_JSON_FORMAT);
        }
    }
}
