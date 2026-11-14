package com.datasphere.fusex.exception;

import com.datasphere.fusex.domain.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(QrCodeIndisponivelException.class)
    public ResponseEntity<ErrorResponse> handleQrIndisponivel(QrCodeIndisponivelException ex) {
        return ResponseEntity.status(409).body(new ErrorResponse(ex.getErrorCode().getCodigo(),
                ex.getErrorCode().getMensagem(), ex.getDetalhes()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse corpo = new ErrorResponse(errorCode.getCodigo(), errorCode.getMensagem(), ex.getDetalhes());
        return ResponseEntity.status(errorCode.getStatus()).body(corpo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleCorpoInvalido(HttpMessageNotReadableException ex) {
        ErrorCode errorCode = ErrorCode.DADOS_INVALIDOS;
        ErrorResponse corpo = new ErrorResponse(errorCode.getCodigo(), errorCode.getMensagem(), Map.of());
        return ResponseEntity.status(errorCode.getStatus()).body(corpo);
    }
}
