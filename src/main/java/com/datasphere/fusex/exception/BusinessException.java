package com.datasphere.fusex.exception;

import com.datasphere.fusex.domain.enums.ErrorCode;

import java.util.List;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object detalhes;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, List.of());
    }

    public BusinessException(ErrorCode errorCode, Object detalhes) {
        super(errorCode.getMensagem());
        this.errorCode = errorCode;
        this.detalhes = detalhes != null ? detalhes : List.of();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object getDetalhes() {
        return detalhes;
    }
}