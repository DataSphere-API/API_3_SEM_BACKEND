package com.datasphere.fusex.espelho.dto;

import jakarta.validation.Valid;

import java.util.List;

public record EspelhoRequest(
        Long atendimentoId,
        String ocsId,
        List<@Valid ItemEspelhoRequest> itens
) {}
