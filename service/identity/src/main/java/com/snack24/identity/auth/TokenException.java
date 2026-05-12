package com.snack24.identity.auth;

import com.snack24.common.jpabase.exception.BusinessException;
import com.snack24.common.jpabase.exception.ErrorCode;

public class TokenException extends BusinessException {
    public TokenException(TokenErrorCode code) {
        super(code);
    }

    public TokenException(TokenErrorCode code, Throwable cause) {
        super(code, cause);
    }
}
