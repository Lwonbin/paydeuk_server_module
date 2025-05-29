package com.tower_of_fisa.paydeuk_server_module.domain.entity;
import com.tower_of_fisa.paydeuk_server_module.domain.enums.CardCompany;
import com.tower_of_fisa.paydeuk_server_module.domain.enums.CardType;
import com.tower_of_fisa.paydeuk_server_module.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "card")
public class Card extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "name", length = 30, nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private CardType type;

  @Column(name = "image_url", length = 200, nullable = false)
  private String imageUrl;

  @Column(name = "annual_fee", nullable = false)
  private Long annualFee;

  @Enumerated(EnumType.STRING)
  @Column(name = "company", nullable = false)
  private CardCompany company;

  @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CardBenefit> cardBenefits = new ArrayList<>();

  @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UserCard> userCards = new ArrayList<>();
}
