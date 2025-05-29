package com.tower_of_fisa.paydeuk_server_module.user_card.repository;

import com.tower_of_fisa.paydeuk_server_module.domain.entity.UserCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCardRepository extends JpaRepository<UserCard,Long> {

    @Query("""
    SELECT CASE WHEN COUNT(uc) > 0 THEN true ELSE false END
    FROM UserCard uc
    JOIN CardBenefit cb ON uc.card.id = cb.card.id
    WHERE uc.user.id = :userId
      AND cb.benefit.id = :benefitId
      AND uc.isDefaultCard = true
    """)
    boolean existsDefaultCardForBenefit(Long userId, Long benefitId);

    @Query("""
    SELECT uc.cardToken
    FROM UserCard uc
    JOIN CardBenefit cb ON cb.card.id = uc.card.id
    WHERE cb.benefit.id = :benefitId
""")
    String findCardTokenByBenefitId(Long benefitId);


    @Query("SELECT uc FROM UserCard uc " +
            "JOIN CardBenefit cb ON uc.card.id = cb.card.id " +
            "WHERE uc.user.id = :userId AND cb.benefit.id = :benefitId")
    Optional<UserCard> findByUserIdAndBenefitId(Long userId, Long benefitId);

    @Query("SELECT uc FROM UserCard uc JOIN FETCH uc.card WHERE uc.user.id = :userId AND uc.isDefaultCard = true")
    Optional<UserCard> findByUserIdAndIsDefaultCardTrue(Long userId);

}
