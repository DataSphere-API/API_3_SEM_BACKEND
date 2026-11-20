package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.StatusPreGuia;
import org.springframework.stereotype.Service;

@Service
public class ValidarPreGuiaAguardandoDecisaoService {

    public void validar(PreGuiaModel preGuia) {
        StatusPreGuia status = preGuia.getStatus();
        if (status == StatusPreGuia.APROVADA || status == StatusPreGuia.EMITIDA) {
            throw new BusinessException(ErrorCode.APROVACAO_JA_REGISTRADA);
        }
        if (status != StatusPreGuia.VALIDADA) {
            throw new BusinessException(ErrorCode.PRE_GUIA_NAO_VALIDADA);
        }
    }
}
