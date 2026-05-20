package com.snack24.order.exception;

import com.snack24.common.jpabase.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    MISSING_CALLER_CONTEXT("ORDER_MISSING_CALLER_CONTENXT", HttpStatus.UNAUTHORIZED , "비정상적인 접근입니다.");
    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;
}
