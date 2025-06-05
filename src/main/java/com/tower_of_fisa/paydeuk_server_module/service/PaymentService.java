package com.tower_of_fisa.paydeuk_server_module.service;

import com.tower_of_fisa.paydeuk_server_module.client.CardApiClient;
import com.tower_of_fisa.paydeuk_server_module.domain.entity.CardBenefit;
import com.tower_of_fisa.paydeuk_server_module.domain.entity.Merchant;
import com.tower_of_fisa.paydeuk_server_module.domain.entity.Payment;
import com.tower_of_fisa.paydeuk_server_module.domain.entity.UserCard;
import com.tower_of_fisa.paydeuk_server_module.dto.PaymentResponse;
import com.tower_of_fisa.paydeuk_server_module.dto.ProcessPaymentRequest;
import com.tower_of_fisa.paydeuk_server_module.global.config.redis.RedisService;
import com.tower_of_fisa.paydeuk_server_module.repository.CardBenefitRepository;
import com.tower_of_fisa.paydeuk_server_module.repository.MerchantRepository;
import com.tower_of_fisa.paydeuk_server_module.repository.PaymentRepository;
import com.tower_of_fisa.paydeuk_server_module.repository.UserCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    /**
     * [카드 추천] 상품을 결제할 때 가장 많은 혜택을 받을 수 있는 카드를 추천한다.
     *
     * @param request RecommendRequest - 사용자id, 가맹점id, 결제 금액
     * @return Long - 추가된 유저의 ID
     */
    @Transactional
    public void processPayment(ProcessPaymentRequest request) {
        // 걀제 완료 시 레디스에 할인 금액 저장을 위한 키 생성
        String key = "user_benefit:" + request.getUserId() + ":"+LocalDate.now().format(DateTimeFormatter.ofPattern("MM"));

        // 결제를 진행할 카드의 카드 토큰
        String cardToken = userCardRepository.findCardTokenByUserIdAndCardId(request.getUserId(), request.getCardId())
                .orElseThrow(() -> new IllegalArgumentException("카드 토큰이 존재하지 않습니다."));

        // 카드사와의 결제 프로세스 진행
        PaymentResponse paymentResponse = cardApiClient.processPayment(cardToken, request.getAmount(), request.getMerchantId());

            UserCard userCard =
                    userCardRepository
                            .findByUserIdAndCardId(request.getUserId(), request.getCardId())
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User Card 입니다."));

            Merchant merchant = merchantRepository.getReferenceById(request.getMerchantId());

            CardBenefit cardBenefit =
                cardBenefitRepository
                        .findByCardAndBenefitId(userCard.getCard(), paymentResponse.getBenefitId())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카드"));

            // DB에 결제 내역 저장
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
