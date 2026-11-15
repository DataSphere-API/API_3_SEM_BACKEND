package com.datasphere.fusex.espelho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ItemEspelhoRequest(
        @NotBlank(message = "é obrigatório") String beneficiarioId,
        @NotBlank(message = "é obrigatória") String descricao,
        @NotNull(message = "é obrigatório") @Positive(message = "deve ser maior que zero") BigDecimal valor
) {}
