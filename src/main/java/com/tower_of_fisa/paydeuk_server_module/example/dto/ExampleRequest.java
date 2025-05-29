package com.tower_of_fisa.paydeuk_server_module.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Example : Request 스키마") // Swagger Docs 표시 : 스키마 설명
public class ExampleRequest {

  @NotBlank(message = "이름을 입력해주세요.") // Field Validation 처리
  @Schema(description = "이름", example = "My name is blah blah") // Swagger Docs 표시 : 상세 필드 설명
  private String name;
}
