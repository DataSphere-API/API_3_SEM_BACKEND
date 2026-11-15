package com.datasphere.fusex.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * US004-6 — Request do POST /atendimentos/{id}/sessoes
 * Exemplo legado: {"cnpj":"12345678000199"}.
 * O vínculo efetivo é obtido do funcionário autenticado, nunca deste campo.
 */
public record SessaoRequest(@NotBlank(message = "é obrigatório") String cnpj) {
}
