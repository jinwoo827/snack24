package com.snack24.catalog.exception;

import com.snack24.common.jpabase.exception.BusinessException;
import com.snack24.common.jpabase.exception.ErrorCode;

public class CatalogException extends BusinessException {
    public CatalogException(ErrorCode errorCode) {
        super(errorCode);
    }
}
