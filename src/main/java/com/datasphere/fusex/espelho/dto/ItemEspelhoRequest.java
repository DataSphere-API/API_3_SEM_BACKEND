package com.datasphere.fusex.espelho.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ItemEspelhoRequest(
        @NotNull(message = "é obrigatório") Long atendimentoId,
        @NotNull(message = "é obrigatório") @Positive(message = "deve ser maior que zero") BigDecimal valor
) {}
