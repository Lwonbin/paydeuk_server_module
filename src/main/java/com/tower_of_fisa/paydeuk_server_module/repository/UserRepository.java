package com.tower_of_fisa.paydeuk_server_module.repository;

import com.tower_of_fisa.paydeuk_server_module.domain.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByPersonalAuthKey(String personalAuthKey);

  @Query(
          "SELECT u FROM User u WHERE u.role = 'USER'"
  )
  Page<User> findByRole(Pageable pageable);
  Optional<User> findByUsername(String username);
}
