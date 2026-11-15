package com.datasphere.fusex.exception;

import com.datasphere.fusex.domain.enums.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(QrCodeIndisponivelException.class)
    public ResponseEntity<ErrorResponse> handleQrIndisponivel(QrCodeIndisponivelException ex) {
        return ResponseEntity.status(409).body(new ErrorResponse(ex.getErrorCode().getCodigo(),
                ex.getErrorCode().getMensagem(), ex.getDetalhes()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        return responder(ex.getErrorCode(), ex.getDetalhes());
    }

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
                    return responder(codigo, Map.of());
                }
            }
            causa = causa.getCause();
        }
        return handleInesperado(ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleCorpoInvalido(MethodArgumentNotValidException ex) {
        List<String> detalhes = new ArrayList<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            detalhes.add(erro.getField() + ": " + erro.getDefaultMessage());
        }
        for (ObjectError erro : ex.getBindingResult().getGlobalErrors()) {
            detalhes.add(erro.getDefaultMessage());
        }
        return responder(ErrorCode.VALIDACAO, detalhes);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleParametroInvalido(HandlerMethodValidationException ex) {
        List<String> detalhes = new ArrayList<>();
        for (MessageSourceResolvable erro : ex.getAllErrors()) {
            detalhes.add(erro.getDefaultMessage());
        }
        return responder(ErrorCode.VALIDACAO, detalhes);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonIlegivel(HttpMessageNotReadableException ex) {
        return responder(ErrorCode.VALIDACAO, List.of("corpo: JSON inválido ou campo com tipo errado"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleParametroAusente(MissingServletRequestParameterException ex) {
        return responder(ErrorCode.VALIDACAO, List.of(ex.getParameterName() + ": é obrigatório"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTipoErrado(MethodArgumentTypeMismatchException ex) {
        return responder(ErrorCode.VALIDACAO, List.of(ex.getName() + ": valor inválido"));
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class,
            NoSuchElementException.class, EntityNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNaoEncontrado(Exception ex) {
        return responder(ErrorCode.RECURSO_NAO_ENCONTRADO, Map.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMetodo(HttpRequestMethodNotSupportedException ex) {
        return responder(ErrorCode.METODO_NAO_PERMITIDO, Map.of());
    }

    // Sem este handler, o 403 cairia no handler genérico e viraria 500.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAcessoNegado(AccessDeniedException ex) {
        return responder(ErrorCode.ACESSO_NEGADO, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return responder(ErrorCode.ERRO_INTERNO, Map.of());
    }

    private ResponseEntity<ErrorResponse> responder(ErrorCode erro, Object detalhes) {
        ErrorResponse corpo = new ErrorResponse(erro.getCodigo(), erro.getMensagem(), detalhes);
        return ResponseEntity.status(erro.getStatus()).body(corpo);
    }
}
