package com.datasphere.fusex.espelho.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EspelhoRequest(
        @NotNull(message = "é obrigatório") Long atendimentoId,
        @NotBlank(message = "é obrigatório") String ocsId,
        List<@Valid ItemEspelhoRequest> itens
) {}
