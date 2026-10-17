package com.datasphere.fusex.divergencia.dto;

import com.datasphere.fusex.divergencia.SeveridadeDivergencia;

import java.math.BigDecimal;

public record DivergenciaItemResponse(
        Integer espelhoItemId,
        BigDecimal divergencia,
        boolean autorizado,
        SeveridadeDivergencia severidade
) {}