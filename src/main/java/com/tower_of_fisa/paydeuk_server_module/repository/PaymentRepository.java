package com.tower_of_fisa.paydeuk_server_module.repository;

import com.tower_of_fisa.paydeuk_server_module.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
