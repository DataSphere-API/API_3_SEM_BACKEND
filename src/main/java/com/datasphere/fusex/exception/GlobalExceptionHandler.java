package com.datasphere.fusex.exception;

import com.datasphere.fusex.domain.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusiness(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        HttpStatus status = errorCode == ErrorCode.QR_NAO_ENCONTRADO
                ? HttpStatus.NOT_FOUND
                : HttpStatus.UNPROCESSABLE_ENTITY;

        return ResponseEntity
                .status(status)
                .body(Map.of(
                        "codigo", errorCode.getCodigo(),
                        "mensagem", errorCode.getMensagem()));
    }
}