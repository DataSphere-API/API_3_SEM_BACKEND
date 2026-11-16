package com.datasphere.fusex.guia.dto.request;

import com.datasphere.fusex.guia.StatusPainelBeneficiario;

public record ProcedimentoGuiaResponse(
        Long atendimentoId,
        String nome,
        Ocs ocs,
        String status,
        Integer qtdAutorizada,
        long qtdRealizada,
        boolean temQr,
        StatusPainelBeneficiario statusPainel,
        String procedimento,
        long sessoesFeitas,
        Integer sessoesAutorizadas) {

    public record Ocs(String cnpj, String nome) {}
}