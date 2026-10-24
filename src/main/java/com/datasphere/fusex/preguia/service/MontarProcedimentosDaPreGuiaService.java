package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.preguia.PreGuiaProcModel;
import com.datasphere.fusex.preguia.dto.PreGuiaProcedimentoResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MontarProcedimentosDaPreGuiaService {

    private final CalcularValorTotalPreGuiaService calcularValorTotalPreGuiaService;

    public MontarProcedimentosDaPreGuiaService(CalcularValorTotalPreGuiaService calcularValorTotalPreGuiaService) {
        this.calcularValorTotalPreGuiaService = calcularValorTotalPreGuiaService;
    }

    public List<PreGuiaProcedimentoResponse> montar(List<PreGuiaProcModel> procedimentos) {
        List<PreGuiaProcedimentoResponse> respostas = new ArrayList<>();
        for (PreGuiaProcModel procedimento : procedimentos) {
            BigDecimal valorUnitario = calcularValorTotalPreGuiaService.calcularValorUnitario(procedimento);
            BigDecimal valorTotal = calcularValorTotalPreGuiaService.calcularValorDoProcedimento(procedimento);
            respostas.add(new PreGuiaProcedimentoResponse(
                    procedimento.getId(),
                    procedimento.getProcedimentoModel().getId(),
                    procedimento.getProcedimentoModel().getNome(),
                    new PreGuiaProcedimentoResponse.Ocs(
                            procedimento.getOcsModel().getCnpj(), procedimento.getOcsModel().getNome()),
                    procedimento.getQtdAutorizada(),
                    valorUnitario,
                    valorTotal));
        }
        return respostas;
    }
}