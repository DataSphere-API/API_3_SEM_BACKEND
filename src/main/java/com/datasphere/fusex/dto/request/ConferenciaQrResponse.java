package com.datasphere.fusex.dto.request;

import java.time.LocalDate;

public record ConferenciaQrResponse(
        Long atendimentoId,
        Long guiaId,
        Beneficiario beneficiario,
        String procedimento,
        Ocs ocs,
        Integer qtdAutorizada,
        Integer qtdRealizada,
        boolean prontoParaBaixa,
        LocalDate dataValidade,
        String status
) {

    public record Beneficiario(String precCp, String nome) {
    }

    public record Ocs(String cnpj, String nome) {
    }
}