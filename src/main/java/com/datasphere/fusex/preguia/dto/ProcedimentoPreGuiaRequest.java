package com.datasphere.fusex.preguia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record ProcedimentoPreGuiaRequest(
        @NotNull(message = "é obrigatório") @Positive(message = "deve ser maior que zero") Integer procedimentoId,
        @NotBlank(message = "é obrigatório")
        @Pattern(regexp = "\\d{14}", message = "informe os 14 dígitos do CNPJ") String ocsCnpj,
        @NotNull(message = "é obrigatória") @Positive(message = "deve ser maior que zero") Integer quantidade) {
}
