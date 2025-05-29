package com.tower_of_fisa.paydeuk_server_module.domain.entity;
import com.tower_of_fisa.paydeuk_server_module.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "card_benefit")
public class CardBenefit extends BaseEntity {
  @Id
  @Column(name = "id", nullable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "card_id", nullable = false)
  private Card card;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "benefit_id", nullable = false)
  private Benefit benefit;
}
