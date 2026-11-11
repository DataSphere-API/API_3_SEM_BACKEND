package com.datasphere.fusex.dto;

import com.datasphere.fusex.domain.HistoricoStatus;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import java.time.LocalDateTime;

public record HistoricoStatusResponse(
        Long id,
        EntidadeTipo entidadeTipo,
        Long entidadeId,
        String statusAnterior,
        String statusNovo,
        LocalDateTime data,
        String origem
) {
    public static HistoricoStatusResponse from(HistoricoStatus h) {
        return new HistoricoStatusResponse(
                h.getId(), h.getEntidadeTipo(), h.getEntidadeId(),
                h.getStatusAnterior(), h.getStatusNovo(), h.getData(), h.getOrigem());
    }
}