package com.snack24.billing.exception;

import com.snack24.common.jpabase.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BillingErrorCode implements ErrorCode {
    MISSING_CALLER_CONTEXT("BILLING_MISSING_CALLER_CONTENXT", HttpStatus.UNAUTHORIZED , "비정상적인 접근입니다."),
    INSUFFICIENT_BALANCE("BILLING_INSUFFICIENT_BALANCE", HttpStatus.CONFLICT, "잔액이 부족합니다."),
    WALLET_NOT_FOUND("BILLING_WALLET_NOT_FOUND", HttpStatus.NOT_FOUND, "지값 정보가 없습니다."),
    PAYMENT_FAILED("BILLING_PAYMENT_FAILED", HttpStatus.CONFLICT, "결제에 실패하였습니다.");
    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;
}
