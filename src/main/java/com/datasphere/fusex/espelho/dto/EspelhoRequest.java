package com.datasphere.fusex.espelho.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

// A OCS vem do login; o ocsId do corpo, se vier, precisa ser o da OCS logada.
public record EspelhoRequest(
        String ocsId,
        @NotNull(message = "é obrigatória") LocalDate dataInicio,
        @NotNull(message = "é obrigatória") LocalDate dataFim,
        @NotEmpty(message = "escolha pelo menos um atendimento") List<@NotNull(message = "é obrigatório") @Valid ItemEspelhoRequest> itens
) {}
