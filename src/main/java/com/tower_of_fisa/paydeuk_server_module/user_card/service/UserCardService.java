package com.tower_of_fisa.paydeuk_server_module.user_card.service;

import com.tower_of_fisa.paydeuk_server_module.benefit_condition.repository.BenefitConditionRepository;
import com.tower_of_fisa.paydeuk_server_module.discount.repository.DiscountRepository;
import com.tower_of_fisa.paydeuk_server_module.domain.entity.*;
import com.tower_of_fisa.paydeuk_server_module.domain.enums.DiscountApplyType;
import com.tower_of_fisa.paydeuk_server_module.user_card.client.CardApiClient;
import com.tower_of_fisa.paydeuk_server_module.user_card.dto.BenefitDiscount;
import com.tower_of_fisa.paydeuk_server_module.user_card.dto.CardConditionResponse;
import com.tower_of_fisa.paydeuk_server_module.benefit.repository.BenefitRepository;
import com.tower_of_fisa.paydeuk_server_module.user_card.dto.RecommendRequest;
import com.tower_of_fisa.paydeuk_server_module.global.common.ErrorDefineCode;
import com.tower_of_fisa.paydeuk_server_module.global.config.exception.custom.exception.NoSuchElementFoundException404;
import com.tower_of_fisa.paydeuk_server_module.user_card.dto.RecommendResponse;
import com.tower_of_fisa.paydeuk_server_module.user_card.repository.UserCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserCardService {

    private final BenefitRepository benefitRepository;
    private final DiscountRepository discountRepository;
    private final UserCardRepository userCardRepository;
    private final BenefitConditionRepository benefitConditionRepository;
    private final CardApiClient cardApiClient;

        /**
     * [카드 추천] 상품을 결제할 때 가장 많은 혜택을 받을 수 있는 카드를 추천한다.
     *
     * @param request RecommendRequest - 사용자id, 가맹점id, 결제 금액
     * @return Long - 추가된 유저의 ID
     */
    public List<RecommendResponse> recommendCard(RecommendRequest request) {

        // 사용자Id, 가맹점Id로 해당 혜택조회
        List<Benefit> availableBenefits = benefitRepository.findByUserIdAndMerchantId(request.getUserId(), request.getMerchantId());

        if (availableBenefits.isEmpty()) {
            log.info("해당 가맹점에서 혜택을 받을 수 있는카드가 없습니다.");
            return userCardRepository.findByUserIdAndIsDefaultCardTrue(request.getUserId())
                    .map(userCard -> {
                        Card card = userCard.getCard();
                        return List.of(new RecommendResponse(
                                card.getName(),
                                card.getImageUrl(),
                                userCard.getCardNumber(),
                                true,
                                0
                        ));
                    })
                    .orElse(List.of());
        }

        log.info("해당 가맹점에서 사용할 수 있는 혜택들 availableBenefits: {}", availableBenefits);

        // 가장 큰 혜택을 받을 수 있는 순으로 배열에 정렬
        List<BenefitDiscount> sortedDiscounts = calculateAndSortDiscounts(request, availableBenefits);

        // 유효성 검증
        return filterValidCardRecommendations(request, sortedDiscounts);

    }

    private List<BenefitDiscount> calculateAndSortDiscounts(RecommendRequest request, List<Benefit> benefits) {
        List<BenefitDiscount> discounts = new ArrayList<>();

        for (Benefit benefit : benefits) {
            List<Discount> discountDetails = discountRepository.findByBenefitId(benefit.getId());
            if (discountDetails.isEmpty()) {
                log.info("해당 혜택의 할인율을 계산하려고하는데 해당 benefit에 대한 할인 정보가 없습니다.(Discount가 없음) benefitId={}", benefit.getId());
                continue;
            }
            log.info("discountDetails: {}", discountDetails);
            boolean isDefaultCard = userCardRepository.existsDefaultCardForBenefit(request.getUserId(), benefit.getId());

            for (Discount discount : discountDetails) {
                int discountAmount = calculateDiscountAmount(request.getAmount(), discount);

                discounts.add(new BenefitDiscount(benefit.getId(), discount.getSpendingRange().getId(), discountAmount, isDefaultCard));
            }
        }

        discounts.sort((b1, b2) -> {
            int compare = Integer.compare(b2.getDiscount(), b1.getDiscount());
            return compare == 0 ? Boolean.compare(b2.isDefaultCard(), b1.isDefaultCard()) : compare;
        });

        log.info("받을 수 있는 혜택 금액들 discounts: {}", discounts);
        return discounts;
    }

    private List<RecommendResponse> filterValidCardRecommendations(RecommendRequest request, List<BenefitDiscount> discountCandidates) {
        List<RecommendResponse> validRecommendations = new ArrayList<>();
        AtomicInteger maxValidDiscount = new AtomicInteger(-1);

        for (BenefitDiscount discountCandidate : discountCandidates) {
            if (maxValidDiscount.get() > discountCandidate.getDiscount()) break;

            // 혜택 조건 유효성 검증
            Optional<RecommendResponse> result = validateAndBuildResponse(request, discountCandidate);
            result.ifPresent(res -> {
                validRecommendations.add(res);
                maxValidDiscount.set(discountCandidate.getDiscount());
            });
        }

        log.info("유효성 검증에 통과한 헤택들: {}", validRecommendations);
        return validRecommendations;
    }

    private Optional<RecommendResponse> validateAndBuildResponse(RecommendRequest request, BenefitDiscount discount) {
        String cardToken = userCardRepository.findCardTokenByBenefitId(discount.getBenefitId());
        int lastMonthSpending = cardApiClient.getLastMonthSpending(cardToken);

        // 전월 실적에 따른 조건 검증
        // 전월 실적에 해당하는 조건이 아닐시 빈값 리털하여 다음 혜택 유효성 확인
        if (!discountRepository.existsApplicableDiscount(discount.getSpendingRangeId(), lastMonthSpending)) {
            return Optional.empty();
        }

        List<Long> conditionIds = benefitConditionRepository.findConditionIdsByBenefitId(discount.getBenefitId());
        List<CardConditionResponse> conditionUsage = cardApiClient.getBenefitConditions(cardToken, conditionIds);
        List<BenefitCondition> conditionLimits = benefitConditionRepository.findByBenefitId(discount.getBenefitId());

        boolean valid;

        if (conditionUsage.isEmpty()) {
            // 조건 사용 기록이 없을 경우
            valid = conditionLimits.stream()
                    .filter(c -> c.getCategory().name().equals("PER_TRANSACTION_LIMIT"))
                    .filter(c -> c.getSpendingRange() == null || isInSpendingRange(lastMonthSpending, c.getSpendingRange()))
                    .allMatch(c -> discount.getDiscount() <= c.getValue());
        } else {
            // 조건 사용 기록이 있을 경우
            valid = checkConditions(conditionUsage, conditionLimits, discount.getDiscount());
        }

        if (!valid) {
            return Optional.empty();
        }

        return userCardRepository.findByUserIdAndBenefitId(request.getUserId(), discount.getBenefitId())
                .map(card -> new RecommendResponse(
                        card.getCard().getName(),
                        card.getCard().getImageUrl(),
                        card.getCardNumber(),
                        card.getIsDefaultCard(),
                        discount.getDiscount()));
    }

    private int calculateDiscountAmount(float amount, Discount discount) {
        int discountAmount = 0;

        log.info("할인율 계산");
        log.info("amount: {}", amount);
        log.info("discount: {}", discount);
        // 할인 적용 방식에 따라 계산
        if (discount.getApplyType() == DiscountApplyType.RATE) {
            // 할인율이 퍼센트로 적용되는 경우
            discountAmount = (int) Math.floor(amount * (discount.getAmount() / 100));
        } else if (discount.getApplyType() == DiscountApplyType.AMOUNT) {
            // 고정 금액으로 할인되는 경우
            discountAmount = (int) Math.floor(discount.getAmount());
        }
        log.info("discountAmount: {}", discountAmount);
        return discountAmount;
    }



    private boolean checkConditions(List<CardConditionResponse> usedConditions,
                                    List<BenefitCondition> limitConditions,
                                    int discountAmount) {
        Map<Long, BenefitCondition> limitMap = createLimitMap(limitConditions);

        return usedConditions.stream()
                .allMatch(used -> {
                    BenefitCondition limit = limitMap.get(used.getConditionId());
                    return isConditionSatisfied(used, limit, discountAmount);
                });
    }

    private Map<Long, BenefitCondition> createLimitMap(List<BenefitCondition> limitConditions) {
        return limitConditions.stream()
                .collect(Collectors.toMap(BenefitCondition::getId, c -> c));
    }


    private boolean isConditionSatisfied(CardConditionResponse used, BenefitCondition limit, int discountAmount) {
        long max = limit.getValue();
        int usedValue = used.getValue();

        switch (limit.getCategory()) {
            case MONTHLY_LIMIT_COUNT, DAILY_LIMIT_COUNT:
                return usedValue < max;

            case MONTHLY_DISCOUNT_LIMIT, DAILY_DISCOUNT_LIMIT:
                return (max - usedValue) >= discountAmount;

            case PER_TRANSACTION_LIMIT:
                return discountAmount <= max;

            default:
                log.warn("Unknown condition category: {}", limit.getCategory());
                throw new NoSuchElementFoundException404(ErrorDefineCode.BENEFIT_CONDITION_NOT_FOUND);
        }
    }


    private boolean isInSpendingRange(int spending, SpendingRange range) {
        Long min = range.getMinSpending();
        Long max = range.getMaxSpending();
        return (min == null || spending >= min) &&
                (max == null || spending <= max);
    }

}