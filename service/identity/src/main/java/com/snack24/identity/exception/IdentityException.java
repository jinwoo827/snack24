package com.snack24.identity.exception;

import com.snack24.common.jpabase.exception.BusinessException;
import com.snack24.common.jpabase.exception.ErrorCode;

public class IdentityException extends BusinessException {
    public IdentityException(ErrorCode errorCode) {
        super(errorCode);
    }
}
