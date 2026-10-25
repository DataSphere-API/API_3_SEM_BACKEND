package com.datasphere.fusex.encaminhamento.dto;

import com.datasphere.fusex.fatura.StatusFatura;

import java.time.LocalDateTime;

public record EncaminharFaturaResponse(
        Long faturaId,
        StatusFatura status,
        LocalDateTime dataEncaminhamento
) {}