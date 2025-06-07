package com.tower_of_fisa.paydeuk_server_module.domain.entity;
import com.tower_of_fisa.paydeuk_server_module.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment")
public class Payment extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "product_name", length = 30, nullable = false)
  private String productName;

  @Column(name = "amount", nullable = false)
  private Integer amount;

  @Column(name = "payment_success", nullable = false)
  private Boolean paymentSuccess;

  @Column(name = "discount_amount", nullable = false)
  private Integer discountAmount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_card_id", nullable = false)
  private UserCard userCard;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "merchant_id")
  private Merchant merchant;

  @OneToOne
  @JoinColumn(name = "card_benefit_id")
  private CardBenefit cardBenefit;
}
