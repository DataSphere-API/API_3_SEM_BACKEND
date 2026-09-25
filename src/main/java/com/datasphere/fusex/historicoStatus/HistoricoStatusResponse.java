package com.datasphere.fusex.historicoStatus;

import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.HistoricoStatusModel;

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
    public static HistoricoStatusResponse from(HistoricoStatusModel h) {
        return new HistoricoStatusResponse(
                h.getId(), h.getEntidadeTipo(), h.getEntidadeId(),
                h.getStatusAnterior(), h.getStatusNovo(), h.getData(), h.getOrigem());
    }
}
