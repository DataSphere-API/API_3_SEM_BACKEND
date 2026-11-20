package com.datasphere.fusex.guia.dto;

import com.datasphere.fusex.guia.StatusPainelBeneficiario;

public record ProcedimentoGuiaResponse(
        Long atendimentoId,
        String nome,
        Ocs ocs,
        String status,
        Integer qtdAutorizada,
        long qtdRealizada,
        boolean temQr,
        StatusPainelBeneficiario statusPainel) {

    public record Ocs(String cnpj, String nome) {}
}