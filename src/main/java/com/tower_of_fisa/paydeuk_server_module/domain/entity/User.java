package com.tower_of_fisa.paydeuk_server_module.domain.entity;

import com.tower_of_fisa.paydeuk_server_module.domain.enums.UserRole;
import com.tower_of_fisa.paydeuk_server_module.domain.enums.UserStatus;
import com.tower_of_fisa.paydeuk_server_module.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "name", length = 10, nullable = false)
  private String name;

  @Column(name = "username", length = 20, nullable = false)
  private String username;

  @Column(name = "password", length = 60, nullable = false)
  private String password;

  @Column(name = "personal_auth_key", length = 100, nullable = false)
  private String personalAuthKey;

  @Column(name = "phone", length = 20, nullable = false)
  private String phone;

  @Column(name = "email", length = 30)
  private String email;

  @Setter
  @Column(name = "image_url", length = 255)
  private String imageUrl;

  @Column(name = "payment_pin_code", length = 60)
  private String paymentPinCode;

  @Column(name = "address", length = 30)
  private String address;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private UserRole role;

  @Column(name = "birth_date", length = 10, nullable = false) // ex 2000.05.12 (10자리)
  private String birthDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private UserStatus status;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UserCard> userCards = new ArrayList<>();

  public void changePassword(String password) {
    this.password = password;
  }

  public void changeEmail(String email) {
    this.email = email;
  }

  public void changeAddress(String address) {
    this.address = address;
  }

  public void changePaymentPinCode(String paymentPinCode) {
    this.paymentPinCode = paymentPinCode;
  }
}
