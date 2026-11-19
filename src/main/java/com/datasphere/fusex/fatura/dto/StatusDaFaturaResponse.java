package com.datasphere.fusex.fatura.dto;

import com.datasphere.fusex.fatura.StatusFatura;

public record StatusDaFaturaResponse(Long faturaId, StatusFatura status) {
}
