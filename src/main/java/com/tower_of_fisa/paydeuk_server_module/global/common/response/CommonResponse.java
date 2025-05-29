package com.tower_of_fisa.paydeuk_server_module.global.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@Schema(title = "Common response : 기본 응답 객체")
public class CommonResponse<T> {

  @Schema(description = "성공 여부", example = "true")
  private final boolean success;

  @Schema(description = "HTTP Status", example = "OK")
  private final HttpStatus status;

  @Schema(description = "응답 메시지")
  private final String message;

  @Schema(description = "응답 데이터")
  private final T response;
}
