package com.tower_of_fisa.paydeuk_server_module.global.config.exception.custom.exception;

import com.tower_of_fisa.paydeuk_server_module.global.common.ErrorDefineCode;
import com.tower_of_fisa.paydeuk_server_module.global.config.exception.custom.BasicCustomException500;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 503 : 네트워크 오류가 발생했을 경우 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NetworkException503 extends BasicCustomException500 {
  public NetworkException503(ErrorDefineCode code) {
    super(code);
  }
}
