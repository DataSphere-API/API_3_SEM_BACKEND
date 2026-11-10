package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CalcularValorTotalPreGuiaService {

    private final BuscarContratoDaPreGuiaProcService buscarContratoDaPreGuiaProcService;

    public CalcularValorTotalPreGuiaService(BuscarContratoDaPreGuiaProcService buscarContratoDaPreGuiaProcService) {
        this.buscarContratoDaPreGuiaProcService = buscarContratoDaPreGuiaProcService;
    }

    public BigDecimal calcularValorUnitario(PreGuiaProcModel procedimento) {
        ContratoModel contrato = buscarContratoDaPreGuiaProcService.buscarContrato(procedimento);
        if (contrato == null) {
            return BigDecimal.ZERO;
        }
        return contrato.getValor();
    }

    public BigDecimal calcularValorDoProcedimento(PreGuiaProcModel procedimento) {
        BigDecimal valorUnitario = calcularValorUnitario(procedimento);
        return valorUnitario.multiply(BigDecimal.valueOf(procedimento.getQtdAutorizada()));
    }

    public BigDecimal calcularValorTotal(List<PreGuiaProcModel> procedimentos) {
        BigDecimal total = BigDecimal.ZERO;
        for (PreGuiaProcModel procedimento : procedimentos) {
            total = total.add(calcularValorDoProcedimento(procedimento));
        }
        return total;
    }
}