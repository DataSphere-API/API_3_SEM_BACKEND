package com.datasphere.fusex.exception;

import com.datasphere.fusex.domain.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("codigo", errorCode.getCodigo());
        corpo.put("mensagem", ex.getMessage());
        if (!ex.getDetalhes().isEmpty()) {
            corpo.put("detalhes", ex.getDetalhes());
        }

        return ResponseEntity.status(errorCode.getStatus()).body(corpo);
    }
}