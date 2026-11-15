package com.datasphere.fusex.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * US004-6 — Request do POST /atendimentos/{id}/sessoes
 * Exemplo: {"cnpj":"12345678000199"}
 */
public record SessaoRequest(@NotBlank(message = "é obrigatório") String cnpj) {
}
