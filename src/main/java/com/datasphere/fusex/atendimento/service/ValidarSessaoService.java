package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

@Service
public class ValidarSessaoService {

    private static final Set<String> STATUS_GUIA_PERMITIDOS =
            Set.of("EMITIDA", "PARCIALMENTE_REALIZADA");

    public void validarOcsDoContrato(GuiaProcModel atendimento, String cnpjRequest) {
        if (cnpjRequest == null || cnpjRequest.isBlank()) {
            throw new BusinessException(ErrorCode.OCS_DIFERENTE_DO_CONTRATO);
        }

        String cnpjContrato = (atendimento.getContratoModel() != null
                && atendimento.getContratoModel().getOcsModel() != null)
                ? atendimento.getContratoModel().getOcsModel().getCnpj()
                : null;

        if (cnpjContrato == null || !cnpjContrato.equals(cnpjRequest.trim())) {
            throw new BusinessException(ErrorCode.OCS_DIFERENTE_DO_CONTRATO);
        }
    }

    public void validarGuiaEmitida(GuiaProcModel atendimento) {
        GuiaModel guia = atendimento.getGuiaModel();

        if (guia == null || guia.getStatus() == null) {
            throw new BusinessException(ErrorCode.GUIA_NAO_EMITIDA);
        }

        String status = guia.getStatus().toUpperCase();

        if (!STATUS_GUIA_PERMITIDOS.contains(status)) {
            throw new BusinessException(ErrorCode.GUIA_NAO_EMITIDA);
        }
    }

    public void validarGuiaDentroDaValidade(GuiaProcModel atendimento) {
        LocalDate validade = atendimento.getDataValidade();

        if (validade != null && validade.isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.GUIA_EXPIRADA);
        }
    }

    public void validarSaldoDeSessoes(GuiaProcModel atendimento) {
        Integer autorizada = atendimento.getQtdAutorizada();
        Integer realizada = atendimento.getQtdRealizada();

        if (autorizada == null || realizada == null) {
            throw new BusinessException(ErrorCode.SESSOES_ESGOTADAS);
        }

        if (realizada >= autorizada) {
            throw new BusinessException(ErrorCode.SESSOES_ESGOTADAS);
        }
    }
}