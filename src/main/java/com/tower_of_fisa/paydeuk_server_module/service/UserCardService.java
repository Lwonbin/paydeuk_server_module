package com.tower_of_fisa.paydeuk_server_module.service;

import com.tower_of_fisa.paydeuk_server_module.domain.enums.BenefitConditionCategory;
import com.tower_of_fisa.paydeuk_server_module.repository.BenefitConditionRepository;
import com.tower_of_fisa.paydeuk_server_module.repository.DiscountRepository;
import com.tower_of_fisa.paydeuk_server_module.domain.entity.*;
import com.tower_of_fisa.paydeuk_server_module.client.CardApiClient;
import com.tower_of_fisa.paydeuk_server_module.dto.BenefitDiscount;
import com.tower_of_fisa.paydeuk_server_module.dto.CardConditionResponse;
import com.tower_of_fisa.paydeuk_server_module.repository.BenefitRepository;
import com.tower_of_fisa.paydeuk_server_module.dto.RecommendRequest;
import com.tower_of_fisa.paydeuk_server_module.dto.RecommendResponse;
import com.tower_of_fisa.paydeuk_server_module.repository.UserCardRepository;
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
            return getDefaultCardRecommendation(request.getUserId());
        }

        log.info("해당 가맹점에서 사용할 수 있는 혜택들 availableBenefits: {}", availableBenefits);

        // 가장 큰 혜택을 받을 수 있는 순으로 배열에 정렬
        List<BenefitDiscount> sortedDiscounts = calculateAndSortDiscounts(request, availableBenefits);

        // 유효성 검증
        List<RecommendResponse> validRecommendations = filterValidCardRecommendations(request, sortedDiscounts);

        if (validRecommendations.isEmpty()) {
            log.info("혜택 조건(전월 실적,한도)을 만족하는 카드가 없어 대표카드를 추천합니다.");
            return getDefaultCardRecommendation(request.getUserId());
        }

        return validRecommendations;
    }

    private List<BenefitDiscount> calculateAndSortDiscounts(RecommendRequest request, List<Benefit> benefits) {
        List<BenefitDiscount> benefitDiscounts = new ArrayList<>();

        for (Benefit benefit : benefits) {
            List<Discount> discounts = discountRepository.findByBenefitId(benefit.getId());

            log.info("benefit Id: {}의 할인율(discounts)의 개수: {}", benefit.getId(),discounts.size());
            boolean isDefaultCard = userCardRepository.existsDefaultCardForBenefit(request.getUserId(), benefit.getId());

            for (Discount discount : discounts) {
                int discountAmount = calculateDiscountAmount(request.getAmount(), discount);

                benefitDiscounts.add(new BenefitDiscount(benefit.getId(), discount.getSpendingRange().getId(), discountAmount, isDefaultCard));
            }
        }

        benefitDiscounts.sort((b1, b2) -> {
            int compare = Integer.compare(b2.getDiscount(), b1.getDiscount());
            return compare == 0 ? Boolean.compare(b2.isDefaultCard(), b1.isDefaultCard()) : compare;
        });

        log.info("유효검사 전 받을 수 있는 혜택 금액들 discounts: {}", benefitDiscounts);
        return benefitDiscounts;
    }

    private List<RecommendResponse> filterValidCardRecommendations(RecommendRequest request, List<BenefitDiscount> discountCandidates) {
        List<RecommendResponse> validRecommendations = new ArrayList<>();
        AtomicInteger maxDiscount = new AtomicInteger(-1);

        for (BenefitDiscount discount : discountCandidates) {
            log.info("혜택 유효성 검증 시작 benefit_id={}", discount.getBenefitId());

            boolean isNotBest = maxDiscount.get() > discount.getDiscount();
            Optional<BenefitDiscount> validated = isNotBest ? Optional.empty() : validateAndAdjustDiscount(discount);

            if (validated.isPresent()) {
                BenefitDiscount finalDiscount = validated.get();
                log.info("혜택 유효성 검증 통과 benefit_id={}", discount.getBenefitId());
                log.info("혜택 유효성 검증 통과 후 최종 할인적용 금액={}", finalDiscount);

                if (finalDiscount.getDiscount() > maxDiscount.get()) {
                    validRecommendations.clear();
                    maxDiscount.set(finalDiscount.getDiscount());
                }

                userCardRepository.findByUserIdAndBenefitId(request.getUserId(), finalDiscount.getBenefitId())
                        .map(card -> RecommendResponse.from(card, finalDiscount.getDiscount()))
                        .ifPresent(validRecommendations::add);
            }

            if (isNotBest) break;
        }

        log.info("유효성 검증에 통과한 혜택들: {}", validRecommendations);
        return validRecommendations;
    }


    private Optional<BenefitDiscount> validateAndAdjustDiscount(BenefitDiscount benefitDiscount) {
        List<String> cardTokens = userCardRepository.findCardTokenByBenefitId(benefitDiscount.getBenefitId());
        log.info("cardTokens: {}", cardTokens);

        List<Long> conditionIds = benefitConditionRepository.findConditionIdsByBenefitId(benefitDiscount.getBenefitId());
        List<BenefitCondition> conditionLimits = benefitConditionRepository.findByBenefitId(benefitDiscount.getBenefitId());
        boolean hasCondition = benefitRepository.findHasAdditionalConditionById(benefitDiscount.getBenefitId());

        for (String cardToken : cardTokens) {
            if (cardToken.isEmpty()) continue;

            int lastMonthSpending = cardApiClient.getLastMonthSpending(cardToken);

            if (!discountRepository.existsApplicableDiscount(benefitDiscount.getSpendingRangeId(), lastMonthSpending)) {
                log.info("benefit_id={}의 실적이 spendingRange_id={}에 해당하지 않음", benefitDiscount.getBenefitId(), benefitDiscount.getSpendingRangeId());
                continue;
            }
            log.info("benefit_id={}의 실적이 spendingRange_id={}에 해당함", benefitDiscount.getBenefitId(), benefitDiscount.getSpendingRangeId());

            if (Boolean.FALSE.equals(hasCondition)) {
                log.info("benefit_id={}의 추가 한도 조건이 없음", benefitDiscount.getBenefitId());
                return Optional.of(benefitDiscount);
            }

            List<CardConditionResponse> conditionUsage = cardApiClient.getBenefitConditions(cardToken, conditionIds);

            int adjustedAmount = conditionUsage.isEmpty()
                    ? adjustWithoutUsage(benefitDiscount, lastMonthSpending, conditionLimits)
                    : adjustWithUsage(benefitDiscount, conditionUsage, conditionLimits);

            if (adjustedAmount > 0) {
                return Optional.of(BenefitDiscount.fromAdjusted(benefitDiscount, adjustedAmount));
            }
        }

        return Optional.empty();
    }

    private int adjustWithoutUsage(BenefitDiscount benefitDiscount, int spending, List<BenefitCondition> limits) {
        log.info("benefit_id={}의 혜택조건(한도) 사용 기록이 없음", benefitDiscount.getBenefitId());

        OptionalInt minLimit = limits.stream()
                .filter(c -> EnumSet.of(
                        BenefitConditionCategory.PER_TRANSACTION_LIMIT,
                        BenefitConditionCategory.DAILY_DISCOUNT_LIMIT,
                        BenefitConditionCategory.MONTHLY_DISCOUNT_LIMIT
                ).contains(c.getCategory()))
                .filter(c -> c.getSpendingRange() == null || isInSpendingRange(spending, c.getSpendingRange()))
                .mapToInt(c -> c.getValue().intValue())
                .min();

        return Math.min(benefitDiscount.getDiscount(), minLimit.orElse(benefitDiscount.getDiscount()));
    }


    private int adjustWithUsage(BenefitDiscount benefitDiscount, List<CardConditionResponse> usage, List<BenefitCondition> limits) {
        log.info("benefit_id={}의 혜택조건(한도) 사용 기록 있음", benefitDiscount.getBenefitId());
        int adjusted = benefitDiscount.getDiscount();

        Map<Long, BenefitCondition> limitMap = limits.stream()
                .collect(Collectors.toMap(BenefitCondition::getId, l -> l));

        for (CardConditionResponse used : usage) {
            BenefitCondition limit = limitMap.get(used.getConditionId());
            if (limit == null) continue;

            long max = limit.getValue();
            int usedValue = used.getValue();

            switch (limit.getCategory()) {
                case MONTHLY_LIMIT_COUNT,DAILY_LIMIT_COUNT:
                    if (usedValue >= max) return 0;
                    break;

                case MONTHLY_DISCOUNT_LIMIT,DAILY_DISCOUNT_LIMIT:
                    adjusted = Math.min(adjusted, (int) (max - usedValue));
                    break;

                case PER_TRANSACTION_LIMIT:
                    adjusted = Math.min(adjusted, (int) max);
                    break;

                default:
                    break;
            }
        }

        return adjusted;
    }


    private int calculateDiscountAmount(float amount, Discount discount) {

        log.info("할인율 계산");
        log.info("amount: {}", amount);
        log.info("discount 방식: {}{} , ",discount.getAmount(), discount.getApplyType());
        // 할인 적용 방식에 따라 계산
        return switch (discount.getApplyType()) {
            case RATE -> (int) Math.floor(amount * (discount.getAmount() / 100));
            case AMOUNT -> (int) Math.floor(discount.getAmount());
        };
    }

    private boolean isInSpendingRange(int spending, SpendingRange range) {
        Long min = range.getMinSpending();
        Long max = range.getMaxSpending();
        return (min == null || spending >= min) &&
                (max == null || spending <= max);
    }

    private List<RecommendResponse> getDefaultCardRecommendation(Long userId) {
        return userCardRepository.findByUserIdAndIsDefaultCardTrue(userId)
                .map(userCard -> List.of(RecommendResponse.fromDefault(userCard)))
                .orElse(List.of());
    }
}