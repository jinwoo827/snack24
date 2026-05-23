package com.snack24.billing.exception;

import com.snack24.common.jpabase.exception.BusinessException;
import com.snack24.common.jpabase.exception.ErrorCode;

public class BillingException extends BusinessException {
    public BillingException(ErrorCode errorCode) {
        super(errorCode);
    }
}
