package com.datasphere.fusex.contrato.service;

import com.datasphere.fusex.guiaproc.GuiaProcModel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CalcularValorContratadoService {

    // Valor do contrato (por sessão) vezes as sessões previstas na guia. Sem contrato devolve null.
    public BigDecimal calcular(GuiaProcModel atendimento) {
        if (atendimento == null || atendimento.getContratoModel() == null
                || atendimento.getContratoModel().getValor() == null) {
            return null;
        }
        int sessoes = atendimento.getQtdAutorizada() == null ? 1 : atendimento.getQtdAutorizada();
        return atendimento.getContratoModel().getValor().multiply(BigDecimal.valueOf(sessoes));
    }
}
