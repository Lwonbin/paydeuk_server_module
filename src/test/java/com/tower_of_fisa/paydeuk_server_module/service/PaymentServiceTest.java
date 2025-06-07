package com.tower_of_fisa.paydeuk_server_module.service;

import com.tower_of_fisa.paydeuk_server_module.client.CardApiClient;
import com.tower_of_fisa.paydeuk_server_module.domain.entity.*;
import com.tower_of_fisa.paydeuk_server_module.dto.PaymentResponse;
import com.tower_of_fisa.paydeuk_server_module.dto.ProcessPaymentRequest;
import com.tower_of_fisa.paydeuk_server_module.global.config.exception.custom.exception.AuthCredientialException401;
import com.tower_of_fisa.paydeuk_server_module.global.config.redis.RedisService;
import com.tower_of_fisa.paydeuk_server_module.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  // 의존성 Mock 객체 선언
  @Mock private CardApiClient cardApiClient;
  @Mock private RedisService redisService;
  @Mock private UserCardRepository userCardRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private MerchantRepository merchantRepository;
  @Mock private CardBenefitRepository cardBenefitRepository;
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  // 테스트 대상 객체에 Mock 주입
  @InjectMocks private PaymentService paymentService;

  // 테스트용 결제 요청 객체 생성
  private ProcessPaymentRequest createRequest() {
    ProcessPaymentRequest request = new ProcessPaymentRequest();
    ReflectionTestUtils.setField(request, "userId", 1L);
    ReflectionTestUtils.setField(request, "cardId", 2L);
    ReflectionTestUtils.setField(request, "amount", 1000.0);
    ReflectionTestUtils.setField(request, "merchantId", 3L);
    ReflectionTestUtils.setField(request, "productName", "item");
    ReflectionTestUtils.setField(request, "paymentPinCode", "159753");
    return request;
  }

  // 테스트용 사용자 객체 생성
  private User createUser() {
    User user = User.builder().paymentPinCode("hashed").build(); // 비밀번호는 해시 처리된 값
    ReflectionTestUtils.setField(user, "id", 1L);
    return user;
  }

  @Test
  @DisplayName("결제 처리 - 결제 내역 저장 및 혜택 금액 저장")
  void processPayment_savesPayment_andStoresDiscount_whenNoPreviousValue() {
    // given: 사용자 인증, 카드 토큰, 카드, 가맹점, 혜택 등 모든 Mock 설정
    ProcessPaymentRequest request = createRequest();
    User user = createUser();
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("159753", "hashed")).thenReturn(true); // 비밀번호 일치

    when(userCardRepository.findCardTokenByUserIdAndCardId(1L, 2L)).thenReturn(Optional.of("token"));

    // 카드 API 호출 결과 설정
    PaymentResponse response = new PaymentResponse();
    ReflectionTestUtils.setField(response, "benefitId", 10L);
    ReflectionTestUtils.setField(response, "discountAmount", 200.0);
    when(cardApiClient.processPayment("token", 1000.0, 3L)).thenReturn(response);

    // 카드 및 카드-혜택 매핑 설정
    Card card = new Card();
    ReflectionTestUtils.setField(card, "id", 5L);
    UserCard userCard = UserCard.builder().card(card).build();
    when(userCardRepository.findByUserIdAndCardId(1L, 2L)).thenReturn(Optional.of(userCard));

    Merchant merchant = new Merchant();
    when(merchantRepository.getReferenceById(3L)).thenReturn(merchant);

    CardBenefit cardBenefit = new CardBenefit();
    when(cardBenefitRepository.findByCardAndBenefitId(card, 10L)).thenReturn(Optional.of(cardBenefit));

    // Redis에 기존 값 없음
    when(redisService.getValue(any())).thenReturn("");
    when(redisService.getRemainingSecondsUntilNextTwoMonthsFirstDay()).thenReturn(100L);

    // when: 결제 요청 수행
    paymentService.processPayment(request);

    // then: 결제 내역 저장됨 확인
    ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentRepository).save(captor.capture());
    Payment saved = captor.getValue();
    assertThat(saved.getAmount()).isEqualTo(1000);
    assertThat(saved.getDiscountAmount()).isEqualTo(200);

    // Redis 저장 확인 (기존 값 없었으므로 saveValue만 수행)
    verify(redisService).saveValue(startsWith("user_benefit:"), eq("200.0"), eq(100L));
    verify(redisService, never()).addValue(any(), anyDouble());
  }

  @Test
  @DisplayName("결제 실패 - 간편 결제 비밀번호 틀리면 예외 발생")
  void processPayment_throwsException_whenPinInvalid() {
    // given: 사용자 인증 실패 상황 설정
    ProcessPaymentRequest request = createRequest();
    User user = createUser();
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("159753", "hashed")).thenReturn(false); // 비밀번호 불일치

    // when & then: 예외 발생 검증
    assertThatThrownBy(() -> paymentService.processPayment(request))
            .isInstanceOf(AuthCredientialException401.class);

    // 결제 API 및 저장 로직이 호출되지 않아야 함
    verify(cardApiClient, never()).processPayment(any(), anyDouble(), any());
    verify(paymentRepository, never()).save(any());
  }
}
