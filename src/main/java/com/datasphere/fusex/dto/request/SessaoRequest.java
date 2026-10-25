package com.datasphere.fusex.dto.request;

/**
 * US004-6 — Request do POST /atendimentos/{id}/sessoes
 * Exemplo: {"cnpj":"12345678000199"}
 */
public record SessaoRequest(String cnpj) {
}