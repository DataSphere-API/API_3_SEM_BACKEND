package com.datasphere.fusex.lisura.dto;

import java.math.BigDecimal;

public record LisuraItemResponse(
        Integer espelhoItemId,
        String descricao,
        BigDecimal valorApresentado,
        BigDecimal valorContratado,
        boolean autorizado,
        String motivo
) {}