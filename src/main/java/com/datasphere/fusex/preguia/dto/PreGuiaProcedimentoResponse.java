package com.datasphere.fusex.preguia.dto;

import java.math.BigDecimal;

public record PreGuiaProcedimentoResponse(
        Long atendimentoId,
        Integer procedimentoId,
        String nome,
        Ocs ocs,
        int qtdSessoes,
        BigDecimal valorUnitario,
        BigDecimal valorTotal) {

    public record Ocs(String cnpj, String nome) {}
}