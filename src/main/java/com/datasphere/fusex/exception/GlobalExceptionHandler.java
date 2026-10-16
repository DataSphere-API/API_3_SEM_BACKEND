package com.datasphere.fusex.exception;

import com.datasphere.fusex.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;
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

        HttpStatus status = errorCode == ErrorCode.QR_NAO_ENCONTRADO
                ? HttpStatus.NOT_FOUND
                : HttpStatus.UNPROCESSABLE_ENTITY;

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("codigo", errorCode.getCodigo());
        corpo.put("mensagem", errorCode.getMensagem());
        if (!ex.getDetalhes().isEmpty()) {
            corpo.put("detalhes", ex.getDetalhes());
        }

        return ResponseEntity.status(status).body(corpo);
    }
}