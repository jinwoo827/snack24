package com.snack24.identity.auth;

import com.snack24.common.jpabase.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TokenErrorCode implements ErrorCode {
    ACCESS_TOKEN_EXPIRED("AUTH_ACCESS_TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED, "엑세스 토큰이 만료되었습니다."),
    ACCESS_TOKEN_INVALID("AUTH_ACCESS_TOKEN_INVALID", HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_INVALID("AUTH_REFRESS_TOKEN_INVALID", HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    ;

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;
}
