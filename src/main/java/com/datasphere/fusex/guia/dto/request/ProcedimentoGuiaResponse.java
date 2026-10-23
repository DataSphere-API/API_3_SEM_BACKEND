package com.datasphere.fusex.guia.dto.request;

public record ProcedimentoGuiaResponse(
        Long atendimentoId,
        String nome,
        Ocs ocs,
        String status,
        Integer qtdAutorizada,
        long qtdRealizada,
        boolean temQr) {

    public record Ocs(String cnpj, String nome) {}
}