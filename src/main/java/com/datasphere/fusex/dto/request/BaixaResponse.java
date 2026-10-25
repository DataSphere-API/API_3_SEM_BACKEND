package com.datasphere.fusex.dto.request;

import java.time.LocalDateTime;

public record BaixaResponse(
        Long atendimentoId,
        String statusProcedimento,
        Integer qtdAutorizada,
        Integer qtdRealizada,
        LocalDateTime dataValidacao,
        String statusGuia
) {
}