package com.example.weblog.common.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;

    public BizException(BaseExceptionInterface baseExceptionInterface) {
        super(baseExceptionInterface.getErrorMessage());
        this.errorCode = baseExceptionInterface.getErrorCode();
        this.errorMessage = baseExceptionInterface.getErrorMessage();
    }
}
