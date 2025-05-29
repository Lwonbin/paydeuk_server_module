package com.tower_of_fisa.paydeuk_server_module.repository;

import com.tower_of_fisa.paydeuk_server_module.domain.entity.BenefitCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BenefitConditionRepository extends JpaRepository<BenefitCondition,Long> {
    @Query("SELECT bc.id FROM BenefitCondition bc WHERE bc.benefit.id = :benefitId")
    List<Long> findConditionIdsByBenefitId(Long benefitId);

    List<BenefitCondition> findByBenefitId(Long benefitId);
}
