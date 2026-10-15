package com.datasphere.fusex.guia.dto.request;

import java.math.BigDecimal;

public record PreGuiaProcedimentoResponse(
        Long atendimentoId,
        String nome,
        Ocs ocs,
        Integer qtdSessoes,
        BigDecimal valorUnitario,
        BigDecimal valorTotal) {

    public record Ocs(String cnpj, String nome) {}
}