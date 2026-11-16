package com.datasphere.fusex.exception;

import com.datasphere.fusex.domain.enums.ErrorCode;

/** Conflito na consulta do beneficiário; preserva os códigos HTTP das outras rotas. */
public class QrCodeIndisponivelException extends BusinessException {

    public QrCodeIndisponivelException(ErrorCode errorCode) {
        super(errorCode);
    }
}
