package com.datasphere.fusex.preguia.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record PreGuiaRequest(
        @NotBlank(message = "é obrigatório") String beneficiarioPrecCp,
        @NotNull(message = "é obrigatório") @Positive(message = "deve ser maior que zero") Integer medicoId,
        @NotEmpty(message = "informe pelo menos um procedimento")
        List<@NotNull(message = "é obrigatório") @Valid ProcedimentoPreGuiaRequest> procedimentos) {
}
