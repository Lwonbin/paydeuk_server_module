package com.tower_of_fisa.paydeuk_server_module.service;

import com.tower_of_fisa.paydeuk_server_module.client.CardApiClient;
import com.tower_of_fisa.paydeuk_server_module.domain.entity.*;
import com.tower_of_fisa.paydeuk_server_module.domain.enums.DiscountApplyType;
import com.tower_of_fisa.paydeuk_server_module.dto.RecommendRequest;
import com.tower_of_fisa.paydeuk_server_module.dto.RecommendResponse;
import com.tower_of_fisa.paydeuk_server_module.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCardServiceTest {

  // 각종 의존성 객체를 Mock으로 선언
  @Mock
  private BenefitRepository benefitRepository;
  @Mock
  private DiscountRepository discountRepository;
  @Mock
  private UserCardRepository userCardRepository;
  @Mock
  private CardApiClient cardApiClient;

  // 테스트 대상 클래스에 Mock 객체들을 주입
  @InjectMocks
  private UserCardService userCardService;

  // 테스트용 RecommendRequest 생성 메서드
  private RecommendRequest createRequest() {
    RecommendRequest request = new RecommendRequest();
    ReflectionTestUtils.setField(request, "userId", 1L);
    ReflectionTestUtils.setField(request, "merchantId", 2L);
    ReflectionTestUtils.setField(request, "amount", 10000);
    return request;
  }

  @Test
  @DisplayName("카드 추천 - 혜택 있는 카드가 없는 경우 대표 카드 반환")
  void recommendCard_returnsDefault_whenNoBenefits() {
    // given
    RecommendRequest request = createRequest();
    // 사용자에게 혜택이 없을 경우
    when(benefitRepository.findByUserIdAndMerchantId(1L, 2L)).thenReturn(List.of());

    // 기본 카드(Mock 데이터) 설정
    Card card = new Card();
    ReflectionTestUtils.setField(card, "name", "default");
    ReflectionTestUtils.setField(card, "imageUrl", "img");
    UserCard defaultCard = UserCard.builder()
            .card(card)
            .cardNumber("1234")
            .build();
    when(userCardRepository.findByUserIdAndIsDefaultCardTrue(1L)).thenReturn(Optional.of(defaultCard));

    // when
    List<RecommendResponse> result = userCardService.recommendCard(request);

    // then
    assertThat(result).hasSize(1); // 기본 카드 하나가 추천됨
    verify(benefitRepository).findByUserIdAndMerchantId(1L, 2L); // 혜택 조회 호출 여부 검증
  }

  @Test
  @DisplayName("카드 추천 - 혜택 있는 카드가 있는 경우 추천 카드 반환")
  void recommendCard_returnsValidRecommendation() {
    // given
    RecommendRequest request = createRequest();

    // 혜택 존재 설정
    Benefit benefit = new Benefit();
    ReflectionTestUtils.setField(benefit, "id", 11L);
    when(benefitRepository.findByUserIdAndMerchantId(1L, 2L)).thenReturn(List.of(benefit));

    // 할인 정보 설정
    Discount discount = new Discount();
    ReflectionTestUtils.setField(discount, "benefit", benefit);
    SpendingRange range = new SpendingRange();
    ReflectionTestUtils.setField(range, "id", 22L);
    ReflectionTestUtils.setField(discount, "spendingRange", range);
    ReflectionTestUtils.setField(discount, "applyType", DiscountApplyType.AMOUNT);
    ReflectionTestUtils.setField(discount, "amount", 1000f);
    when(discountRepository.findByBenefitId(11L)).thenReturn(List.of(discount));

    // 기본 카드 아님
    when(userCardRepository.existsDefaultCardForBenefit(1L, 11L)).thenReturn(false);

    // 카드 사용 금액 조건 만족 설정
    when(userCardRepository.findCardTokenByBenefitId(11L)).thenReturn("token");
    when(cardApiClient.getLastMonthSpending("token")).thenReturn(0);
    when(discountRepository.existsApplicableDiscount(22L, 0)).thenReturn(true);
    when(benefitRepository.findHasAdditionalConditionById(11L)).thenReturn(false);

    // 카드 정보 설정
    Card card = new Card();
    UserCard userCard = UserCard.builder().card(card).build();
    when(userCardRepository.findByUserIdAndBenefitId(1L, 11L)).thenReturn(Optional.of(userCard));

    // when
    List<RecommendResponse> result = userCardService.recommendCard(request);

    // then
    assertThat(result).hasSize(1); // 유효한 카드 추천 하나
    assertThat(result.get(0).getDiscountAmount()).isEqualTo(1000); // 할인 금액 검증
  }
}
