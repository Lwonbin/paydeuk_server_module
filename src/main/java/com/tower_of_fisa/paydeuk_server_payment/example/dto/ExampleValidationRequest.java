package com.tower_of_fisa.paydeuk_server_payment.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Example : Validation 예제 스키마") // Swagger Docs 표시 : 스키마 설명
public class ExampleValidationRequest {

  @NotNull(message = "발생시킬 에러 유형은 필수입니다.")
  @Schema(
      description = "에러 유형 (500, 404, 403, 415), 그 외 정상 ",
      example = "404",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer errorCode;

  @NotBlank(message = "이름은 필수 입력값입니다.")
  @Schema(description = "이름", example = "Kim", requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;

  @NotBlank(message = "비밀번호는 필수 입력값입니다.")
  @Pattern(
      regexp = "(?=.*\\d)(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}",
      message = "비밀번호는 영문 대,소문자와 숫자, 특수기호가 적어도 1개 이상씩 포함된 8자 ~ 20자의 비밀번호여야 합니다.")
  @Schema(description = "비밀번호", example = "test123!", requiredMode = Schema.RequiredMode.REQUIRED)
  private String password;

  @Email(message = "이메일 형식에 맞지 않습니다.")
  @Schema(description = "이메일", example = "sjh9708@gmail.com")
  private String email;
}
