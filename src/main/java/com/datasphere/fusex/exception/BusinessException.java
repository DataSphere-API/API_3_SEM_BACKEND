package com.datasphere.fusex.exception;

import com.datasphere.fusex.domain.enums.ErrorCode;

import java.util.Map;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> detalhes;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, Map.of());
    }

    public BusinessException(ErrorCode errorCode, Map<String, Object> detalhes) {
        super(errorCode.getMensagem());
        this.errorCode = errorCode;
        this.detalhes = detalhes;
    }

    // Mensagem específica do caso (ex.: nome do procedimento sem contrato).
    public BusinessException(ErrorCode errorCode, String mensagem) {
        super(mensagem);
        this.errorCode = errorCode;
        this.detalhes = Map.of();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetalhes() {
        return detalhes;
    }
}