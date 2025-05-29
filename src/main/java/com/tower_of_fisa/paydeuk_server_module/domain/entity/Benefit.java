package com.tower_of_fisa.paydeuk_server_module.domain.entity;

import com.tower_of_fisa.paydeuk_server_module.domain.enums.BenefitType;
import com.tower_of_fisa.paydeuk_server_module.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "benefit")
public class Benefit extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "description", length = 100)
  private String description;

  @Column(name = "title", length = 100)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(name = "benefit_type", nullable = false)
  private BenefitType benefitType;

  @Column(name = "has_additional_condition", nullable = false)
  private Boolean hasAdditionalCondition;

  @OneToMany(mappedBy = "benefit", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<BenefitCondition> benefitConditions = new ArrayList<>();

  @OneToMany(mappedBy = "benefit", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CardBenefit> cardBenefits = new ArrayList<>();

  @OneToMany(mappedBy = "benefit", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Discount> discounts = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "merchant_id")
  private Merchant merchant;
}
