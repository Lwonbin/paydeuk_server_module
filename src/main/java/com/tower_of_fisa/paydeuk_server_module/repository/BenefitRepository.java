package com.tower_of_fisa.paydeuk_server_module.repository;

import com.tower_of_fisa.paydeuk_server_module.domain.entity.Benefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@org.springframework.stereotype.Repository
public interface BenefitRepository extends JpaRepository<Benefit, Long> {

    @Query("SELECT b FROM Benefit b " +
            "JOIN CardBenefit cb ON cb.benefit.id = b.id " +
            "JOIN UserCard uc ON uc.card.id = cb.card.id " +
            "WHERE uc.user.id = :userId " +
            "AND b.merchant.id = :merchantId " )
    List<Benefit> findByUserIdAndMerchantId(Long userId, Long merchantId);
}
