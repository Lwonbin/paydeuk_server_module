package com.tower_of_fisa.paydeuk_server_module.service;

import com.tower_of_fisa.paydeuk_server_module.client.CardApiClient;
import com.tower_of_fisa.paydeuk_server_module.domain.entity.*;
import com.tower_of_fisa.paydeuk_server_module.dto.PaymentResponse;
import com.tower_of_fisa.paydeuk_server_module.dto.ProcessPaymentRequest;
import com.tower_of_fisa.paydeuk_server_module.global.common.ErrorDefineCode;
import com.tower_of_fisa.paydeuk_server_module.global.config.exception.custom.exception.AuthCredientialException401;
import com.tower_of_fisa.paydeuk_server_module.global.config.redis.RedisService;
import com.tower_of_fisa.paydeuk_server_module.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final CardApiClient cardApiClient;
    private final RedisService redisService;
    private final UserCardRepository userCardRepository;
    private final PaymentRepository paymentRepository;
    private final MerchantRepository merchantRepository;
    private final CardBenefitRepository cardBenefitRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * [카드 추천] 상품을 결제할 때 가장 많은 혜택을 받을 수 있는 카드를 추천한다.
     *
     * @param request RecommendRequest - 사용자id, 가맹점id, 결제 금액
     */
    @Transactional
    public void processPayment(ProcessPaymentRequest request) {
        log.info("[processPayment] 결제 요청 수신: userId={}, merchantId={}, amount={}, cardId={}, productName={}",
                request.getUserId(), request.getMerchantId(), request.getAmount(),
                request.getCardId(), request.getProductName());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getPaymentPinCode(), user.getPaymentPinCode())) {
            log.warn("[processPayment] 결제 실패 - 비밀번호 불일치: userId={}", request.getUserId());
            throw new AuthCredientialException401(ErrorDefineCode.INVALID_PAYMENT_PIN);
        }

        String cardToken = userCardRepository.findCardTokenByUserIdAndCardId(request.getUserId(), request.getCardId())
                .orElseThrow(() -> new IllegalArgumentException("카드 토큰이 존재하지 않습니다."));

        log.info("[processPayment] 카드사 결제 요청 전송: cardTokenPrefix={}, amount={}, merchantId={}",
                cardToken.substring(0, 6) + "****", request.getAmount(), request.getMerchantId());

        PaymentResponse paymentResponse = cardApiClient.processPayment(cardToken, request.getAmount(), request.getMerchantId());

        log.info("[processPayment] 카드사 응답 수신 성공: response={}", paymentResponse);

        UserCard userCard = userCardRepository
                .findByUserIdAndCardId(request.getUserId(), request.getCardId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User Card 입니다."));

        Merchant merchant = merchantRepository.getReferenceById(request.getMerchantId());

        CardBenefit cardBenefit = cardBenefitRepository
                .findByCardAndBenefitId(userCard.getCard(), paymentResponse.getBenefitId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카드"));

        Payment newPayment = Payment.builder()
                .productName(request.getProductName())
                .amount(Integer.parseInt(String.valueOf(request.getAmount()).split("\\.")[0]))
                .paymentSuccess(true)
                .discountAmount(Integer.parseInt(String.valueOf(paymentResponse.getDiscountAmount()).split("\\.")[0]))
                .userCard(userCard)
                .merchant(merchant)
                .cardBenefit(cardBenefit)
                .build();

        paymentRepository.save(newPayment);

        String key = "user_benefit:" + request.getUserId() + ":" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("MM"));

        log.info("[결제 성공] paymentId={}, userId={}, merchantId={}, productName={}, 결제금액={}, 할인금액={}, cardId={}, benefitTitle={}",
                newPayment.getId(), request.getUserId(), request.getMerchantId(), request.getProductName(),
                newPayment.getAmount(), newPayment.getDiscountAmount(), request.getCardId(),
                cardBenefit.getBenefit().getTitle());

            // 아직 레디스에 할인 금액이 저장되어 있지 않을 경우 (매월 첫 번째 결제)
            if (redisService.getValue(key).isEmpty()) {
                long ttl = redisService.getRemainingSecondsUntilNextTwoMonthsFirstDay();
                redisService.saveValue(key, paymentResponse.getDiscountAmount().toString(), ttl);
            }
            else {
                redisService.addValue(key, paymentResponse.getDiscountAmount());
            }

            log.info("결제 정보 저장 완료됨: paymentId={}", newPayment.getId());

    }
}
