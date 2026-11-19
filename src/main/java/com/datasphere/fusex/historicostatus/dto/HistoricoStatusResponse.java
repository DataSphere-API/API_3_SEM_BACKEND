package com.datasphere.fusex.historicostatus.dto;

import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.HistoricoStatusModel;

import java.time.LocalDateTime;

public record HistoricoStatusResponse(
        Long id,
        EntidadeTipo entidadeTipo,
        Long entidadeId,
        String statusAnterior,
        String statusNovo,
        LocalDateTime data,
        String origem,
        Integer usuarioId,
        String usuarioNome
) {
    public static HistoricoStatusResponse from(HistoricoStatusModel h) {
        return new HistoricoStatusResponse(
                h.getId(), h.getEntidadeTipo(), h.getEntidadeId(),
                h.getStatusAnterior(), h.getStatusNovo(), h.getData(), h.getOrigem(),
                h.getUsuarioId(), h.getUsuarioNome());
    }
}
