package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.preguia.PreGuiaProcModel;
import com.datasphere.fusex.preguia.dto.PreGuiaProcedimentoResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MontarProcedimentosDaPreGuiaService {

    private final CalcularValorTotalPreGuiaService calcularValorTotalPreGuiaService;
    private final BuscarAtendimentosDaPreGuiaService buscarAtendimentosDaPreGuiaService;

    public MontarProcedimentosDaPreGuiaService(CalcularValorTotalPreGuiaService calcularValorTotalPreGuiaService,
                                               BuscarAtendimentosDaPreGuiaService buscarAtendimentosDaPreGuiaService) {
        this.calcularValorTotalPreGuiaService = calcularValorTotalPreGuiaService;
        this.buscarAtendimentosDaPreGuiaService = buscarAtendimentosDaPreGuiaService;
    }

    public List<PreGuiaProcedimentoResponse> montar(List<PreGuiaProcModel> procedimentos) {
        Map<Long, Long> atendimentos = buscarAtendimentosDaPreGuiaService.buscar(procedimentos);
        List<PreGuiaProcedimentoResponse> respostas = new ArrayList<>();
        for (PreGuiaProcModel procedimento : procedimentos) {
            BigDecimal valorUnitario = calcularValorTotalPreGuiaService.calcularValorUnitario(procedimento);
            BigDecimal valorTotal = calcularValorTotalPreGuiaService.calcularValorDoProcedimento(procedimento);
            respostas.add(new PreGuiaProcedimentoResponse(
                    atendimentos.get(procedimento.getId()),
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