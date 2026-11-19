package com.datasphere.fusex.espelho.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

// Na correção a OCS só muda o valor cobrado; beneficiário e procedimento vêm da guia.
public record EspelhoItemPatchRequest(
        @NotNull(message = "é obrigatório") @Positive(message = "deve ser maior que zero") BigDecimal valor) {
}
