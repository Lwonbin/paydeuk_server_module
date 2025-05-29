package com.tower_of_fisa.paydeuk_server_module.domain.entity;
import com.tower_of_fisa.paydeuk_server_module.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "EXAMPLE")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Example extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "EXAM_ID")
  private Long examId;

  @Column(name = "NAME", nullable = false)
  private String name;
}
