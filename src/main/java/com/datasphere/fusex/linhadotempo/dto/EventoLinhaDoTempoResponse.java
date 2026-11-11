package com.datasphere.fusex.linhadotempo.dto;

import com.datasphere.fusex.historicoStatus.EntidadeTipo;

import java.time.LocalDateTime;

public record EventoLinhaDoTempoResponse(
        EtapaLinhaDoTempo etapa,
        EntidadeTipo entidadeTipo,
        Long entidadeId,
        String statusAnterior,
        String statusNovo,
        String descricao,
        LocalDateTime data,
        String origem) {}