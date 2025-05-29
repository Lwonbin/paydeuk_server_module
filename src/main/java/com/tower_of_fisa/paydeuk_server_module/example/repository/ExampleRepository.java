package com.tower_of_fisa.paydeuk_server_module.example.repository;

import com.tower_of_fisa.paydeuk_server_module.domain.entity.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface ExampleRepository extends JpaRepository<Example, Long> {

  // Spring Data JPA 사용
  // 메서드 명명을 통한 기본적인 CRUD 작업은 Spring Data JPA를 사용하여 구현
  Example findFirstByName(String name);

  // JPQL 사용
  // Spring Data JPA로는 달성할 수 없는 복잡한 쿼리는 JPQL을 사용
  @Query("SELECT e FROM Example e WHERE e.name LIKE %:keyword%")
  List<Example> findByKeyword(@Param("keyword") String keyword);
}
