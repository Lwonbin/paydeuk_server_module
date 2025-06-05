package com.tower_of_fisa.paydeuk_server_module.repository;

import com.tower_of_fisa.paydeuk_server_module.domain.entity.Card;
import com.tower_of_fisa.paydeuk_server_module.domain.entity.CardBenefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardBenefitRepository extends JpaRepository<CardBenefit, Long> {

    @Query("""
    SELECT cb
    FROM CardBenefit cb
    WHERE cb.card = :card
      AND cb.benefit.id = :benefitId
""")
    Optional<CardBenefit> findByCardAndBenefitId(
            @Param("card") Card card,
            @Param("benefitId") Long benefitId
    );
}
