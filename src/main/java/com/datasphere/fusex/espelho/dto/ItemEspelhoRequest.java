package com.datasphere.fusex.espelho.dto;

import java.math.BigDecimal;

public record ItemEspelhoRequest(
        String beneficiarioId,
        String descricao,
        BigDecimal valor
) {}