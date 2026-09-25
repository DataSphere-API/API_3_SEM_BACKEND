package com.datasphere.fusex.espelho.request;

import java.math.BigDecimal;

public record ItemEspelhoRequest(
        String beneficiarioId,
        String descricao,
        BigDecimal valor
) {}