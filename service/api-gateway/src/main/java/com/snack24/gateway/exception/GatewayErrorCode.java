package com.snack24.gateway.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GatewayErrorCode {
    AUTH_REQUIRED("AUTH_REQURIED", "인증 헤더가 필요합니다."),
    ACCESS_TOKEN_EXPIRED("AUTH_ACCESS_TOKEN_EXPIRED", "엑세스 토큰이 만료되었습니다."),
    ACCESS_TOKEN_INVALID("AUTH_ACCESS_TOKEN_INVALID", "유효하지 않은 토큰입니다."),
    ;

    private final String code;
    private final String defaultMessage;



}
