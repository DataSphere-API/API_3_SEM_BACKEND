package com.datasphere.fusex.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AtendimentoResponse(
        Long atendimentoId,
        Long guiaId,
        Beneficiario beneficiario,
        String procedimento,
        Ocs ocs,
        Integer qtdAutorizada,
        Integer qtdRealizada,
        String statusProcedimento,
        LocalDate dataValidade,
        LocalDateTime dataRealizacao
) {

    public record Beneficiario(String precCp, String nome) {
    }

    public record Ocs(String cnpj, String nome) {
    }
}