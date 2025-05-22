package com.tower_of_fisa.paydeuk_server_payment.domain.entity;

import com.tower_of_fisa.paydeuk_server_payment.domain.enums.DiscountApplyType;
import com.tower_of_fisa.paydeuk_server_payment.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "discount")
public class Discount extends BaseEntity {
  @Id
  @Column(name = "id", nullable = false)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "apply_type", nullable = false)
  private DiscountApplyType applyType;

  @Column(name = "amount", nullable = false)
  private Float amount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "benefit_id", nullable = false)
  private Benefit benefit;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "spending_range_id", nullable = false)
  private SpendingRange spendingRange;
}
