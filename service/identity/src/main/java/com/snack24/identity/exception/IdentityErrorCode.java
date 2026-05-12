package com.snack24.identity.exception;

import com.snack24.common.jpabase.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum IdentityErrorCode implements ErrorCode {

    EMAIL_DUPLICATED    ("IDENTITY_EMAIL_DUPLICATED",HttpStatus.CONFLICT, "이미 사용중인 이메일입니다."),
    COMPANY_NOT_FOUND   ("IDENTITY_COMPANY_NOT_FOUND",HttpStatus.NOT_FOUND, "회사를 찾을 수 없습니다."),
    DEPARTMENT_NOT_FOUND ("IDENTITY_DEPARTMENT_NOT_FOUND", HttpStatus.NOT_FOUND, "부서를 찾을 수 없습니다."),
    DEPARTMENT_COMPANY_MISMATCH("IDENTITY_DEPARTMENT_COMPANY_MISMATCH", HttpStatus.BAD_REQUEST, "부서가 해당 회사 소속이 아닙니다."),
    MEMBER_NOT_FOUND("IDENTITY_MEMBER_NOT_FOUND", HttpStatus.NOT_FOUND, "직원을 찾을 수 없습니다."),
    INVALID_CREDENTIALS("IDENTITY_INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    BUSINESS_NO_DUPLICATED("IDENTITY_BUSINESS_NO_DUPLICATED", HttpStatus.BAD_REQUEST, "사업자번호가 이미 존재합니다.")
    ;

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;

}
