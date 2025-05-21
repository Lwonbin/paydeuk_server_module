package com.tower_of_fisa.paydeuk_server_payment.global.config.exception.custom.exception;

import com.tower_of_fisa.paydeuk_server_payment.global.common.ErrorDefineCode;
import com.tower_of_fisa.paydeuk_server_payment.global.config.exception.custom.BasicCustomException500;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 408 : 요청이 서버에서 처리되기 전에 제한 시간에서 만료 (신청 기간 마감 등..) */
@ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
public class TimeOutException408 extends BasicCustomException500 {
  public TimeOutException408(ErrorDefineCode code) {
    super(code);
  }
}
