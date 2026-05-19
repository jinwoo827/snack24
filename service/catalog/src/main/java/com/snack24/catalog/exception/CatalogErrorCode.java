package com.snack24.catalog.exception;

import com.snack24.common.jpabase.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CatalogErrorCode implements ErrorCode {
    PRODUCT_NOT_FOUND   ("CATALOG_PRODUCT_NOT_FOUND",   HttpStatus.NOT_FOUND,   "상품을 찾을 수 없습니다."),
    STOCK_NOT_FOUND     ("CATALOG_STOCK_NOT_FOUND",     HttpStatus.NOT_FOUND,   "재고를 찾을 수 없습니다."),
    INSUFFICIENT_STOCK  ("CATALOG_INSUFFICIENT_STOCK",  HttpStatus.CONFLICT,    "재고가 부족합니다."),
    ;
    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;
}
