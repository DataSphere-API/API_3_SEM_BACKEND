package com.datasphere.fusex.fatura.dto;

import jakarta.validation.constraints.NotNull;

public record FaturaRequest(@NotNull(message = "é obrigatório") Integer espelhoId) {}
