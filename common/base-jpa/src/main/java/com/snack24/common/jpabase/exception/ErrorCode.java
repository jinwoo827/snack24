package com.snack24.common.jpabase.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    String getCode();
    HttpStatus getStatus();
    String getDefaultMessage();

}
