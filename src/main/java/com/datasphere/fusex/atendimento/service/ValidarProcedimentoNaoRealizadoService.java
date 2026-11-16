package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.StatusProcedimento;
import org.springframework.stereotype.Service;

@Service
public class ValidarProcedimentoNaoRealizadoService {

    public void validarProcedimentoNaoRealizado(GuiaProcModel procedimento) {
        String status = procedimento.getStatus();
        if (StatusProcedimento.EXPIRADA.name().equalsIgnoreCase(status) || "EXPIRADO".equalsIgnoreCase(status)) {
            throw new BusinessException(ErrorCode.GUIA_EXPIRADA);
        }
        if (StatusProcedimento.REALIZADA.name().equalsIgnoreCase(status)
                || StatusProcedimento.FATURADA.name().equalsIgnoreCase(status)
                || "REALIZADO".equalsIgnoreCase(status)) {
            throw new BusinessException(ErrorCode.PROCEDIMENTO_JA_REALIZADO);
        }
        if (!StatusProcedimento.PENDENTE.name().equalsIgnoreCase(status)
                && !StatusProcedimento.EM_ANDAMENTO.name().equalsIgnoreCase(status)) {
            throw new BusinessException(ErrorCode.STATUS_INVALIDO);
        }
    }
}
