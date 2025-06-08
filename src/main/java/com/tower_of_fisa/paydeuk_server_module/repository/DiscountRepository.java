package com.tower_of_fisa.paydeuk_server_module.repository;

import com.tower_of_fisa.paydeuk_server_module.domain.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    @Query("SELECT d FROM Discount d " +
            "WHERE d.benefit.id = :benefitId")
    List<Discount> findByBenefitId(Long benefitId);

    @Query("""
    SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
    FROM Discount d
    WHERE d.spendingRange.id = :spendingRangeId
      AND (:spending >= d.spendingRange.minSpending OR d.spendingRange.minSpending IS NULL)
      AND (:spending < d.spendingRange.maxSpending OR d.spendingRange.maxSpending IS NULL)
""")
    boolean existsApplicableDiscount(Long spendingRangeId, int spending);


}
