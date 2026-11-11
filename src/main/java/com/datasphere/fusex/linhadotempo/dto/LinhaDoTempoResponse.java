package com.datasphere.fusex.linhadotempo.dto;

import java.time.LocalDate;
import java.util.List;

public record LinhaDoTempoResponse(
        Long guiaId,
        String statusAtual,
        LocalDate dataEmissao,
        List<EventoLinhaDoTempoResponse> eventos) {}