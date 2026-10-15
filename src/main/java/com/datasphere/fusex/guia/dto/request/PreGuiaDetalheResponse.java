package com.datasphere.fusex.guia.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record PreGuiaDetalheResponse(
        Long id,
        String status,
        Beneficiario beneficiario,
        Medico medico,
        List<PreGuiaProcedimentoResponse> procedimentos,
        BigDecimal valorTotal,
        Validacao validacao) {

    public record Beneficiario(String precCp, String nome) {}

    public record Medico(String crm) {}

    public record Validacao(boolean validada, List<Pendencia> pendencias) {
        public record Pendencia(String codigo, Long atendimentoId, String mensagem) {}
    }
}