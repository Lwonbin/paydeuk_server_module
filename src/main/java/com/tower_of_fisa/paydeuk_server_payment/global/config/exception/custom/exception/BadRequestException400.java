package com.tower_of_fisa.paydeuk_server_payment.global.config.exception.custom.exception;

import com.tower_of_fisa.paydeuk_server_payment.global.common.ErrorDefineCode;
import com.tower_of_fisa.paydeuk_server_payment.global.config.exception.custom.BasicCustomException500;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 400 : 잘못된 요청 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException400 extends BasicCustomException500 {
  public BadRequestException400(ErrorDefineCode code) {
    super(code);
  }
}
