package com.datasphere.fusex.divergencia.dto;

import java.math.BigDecimal;
import java.util.List;

public record DivergenciasResponse(
        Long faturaId,
        List<DivergenciaItemResponse> itens,
        BigDecimal totalDivergencia
) {}