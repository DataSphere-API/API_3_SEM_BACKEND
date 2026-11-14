package com.datasphere.fusex.exception;

import com.datasphere.fusex.domain.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.exception.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegridade(DataIntegrityViolationException ex) {
        Throwable causa = ex;
        while (causa != null) {
            if (causa instanceof ConstraintViolationException violacao && violacao.getConstraintName() != null) {
                ErrorCode codigo = switch (violacao.getConstraintName()) {
                    case "pk_ocs" -> ErrorCode.OCS_JA_CADASTRADA;
                    case "uq_procedimento_nome" -> ErrorCode.PROCEDIMENTO_JA_CADASTRADO;
                    case "uq_contrato_procedimento_ocs" -> ErrorCode.CONTRATO_DUPLICADO;
                    case "ck_contrato_valor_positivo" -> ErrorCode.VALOR_INVALIDO;
                    case "uq_usuario_login" -> ErrorCode.CPF_JA_CADASTRADO;
                    case "uq_medico_crm" -> ErrorCode.CRM_JA_CADASTRADO;
                    default -> null;
                };
                if (codigo != null) {
                    return handleBusiness(new BusinessException(codigo));
                }
            }
            causa = causa.getCause();
        }
        throw ex;
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
