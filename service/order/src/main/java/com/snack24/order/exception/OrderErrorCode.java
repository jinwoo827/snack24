package com.snack24.order.exception;

import com.snack24.common.jpabase.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    MISSING_CALLER_CONTEXT("ORDER_MISSING_CALLER_CONTENXT", HttpStatus.UNAUTHORIZED , "비정상적인 접근입니다."),
    SAGA_NOT_FOUND("ORDER_SAGA_NOT_FOUND", HttpStatus.NOT_FOUND , "비정상적인 접근입니다."),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", HttpStatus.NOT_FOUND , "비정상적인 접근입니다."),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", HttpStatus.BAD_REQUEST , "비정상적인 접근입니다."),
    CATALOG_UNAVAILABLE("CATALOG_UNAVAILABLE", HttpStatus.SERVICE_UNAVAILABLE, "비정상적인 접근입니다.");
    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;
}
