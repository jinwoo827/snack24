package com.snack24.gateway.exception;

import lombok.Getter;

@Getter
public class GatewayException extends RuntimeException {
    private final GatewayErrorCode errorCode;

    public GatewayException(GatewayErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public GatewayException(GatewayErrorCode errorCode, Throwable cause) {
        super(errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
    }
}
