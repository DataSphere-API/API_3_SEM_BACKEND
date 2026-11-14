package com.datasphere.fusex.espelho.dto;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record EspelhoItemPatchRequest(
        String beneficiarioId,
        String descricao,
        @Positive(message = "deve ser maior que zero") BigDecimal valor) {
}
