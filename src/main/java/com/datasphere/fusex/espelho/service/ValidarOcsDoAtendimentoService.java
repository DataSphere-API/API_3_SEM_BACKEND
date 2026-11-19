package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValidarOcsDoAtendimentoService {

    public void validarOcs(String ocsIdEspelho, GuiaProcModel atendimento) {
        if (ocsIdEspelho == null || ocsIdEspelho.isBlank()) {
            throw new BusinessException(ErrorCode.OCS_DIVERGENTE, List.of("ocsId: é obrigatório"));
        }

        if (atendimento == null || atendimento.getContratoModel() == null
                || atendimento.getContratoModel().getOcsModel() == null) {
            throw new BusinessException(ErrorCode.OCS_DIVERGENTE, List.of("ocsId: atendimento sem contrato ou OCS"));
        }

        String cnpjContrato = atendimento.getContratoModel().getOcsModel().getCnpj();
        if (!ocsIdEspelho.trim().equalsIgnoreCase(cnpjContrato.trim())) {
            throw new BusinessException(ErrorCode.OCS_DIVERGENTE, List.of("ocsId: a OCS informada não corresponde à OCS do contrato do atendimento"));
        }
    }
}
