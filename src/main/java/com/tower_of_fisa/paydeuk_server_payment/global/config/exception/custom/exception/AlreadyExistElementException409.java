package com.tower_of_fisa.paydeuk_server_payment.global.config.exception.custom.exception;

import com.tower_of_fisa.paydeuk_server_payment.global.common.ErrorDefineCode;
import com.tower_of_fisa.paydeuk_server_payment.global.config.exception.custom.BasicCustomException500;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 409 : 이미 존재하는 자원과 중복될 때 */
@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyExistElementException409 extends BasicCustomException500 {
  public AlreadyExistElementException409(ErrorDefineCode code) {
    super(code);
  }
}
