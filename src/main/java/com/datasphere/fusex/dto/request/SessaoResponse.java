package com.datasphere.fusex.dto.request;

import java.time.LocalDateTime;

/**
 * US004-6 — Response do POST /atendimentos/{id}/sessoes (HTTP 201)
 * Exemplo:
 * {
 *   "atendimentoId":10,"numeroSessao":3,"qtdAutorizada":10,"qtdRealizada":3,
 *   "saldo":7,"statusProcedimento":"EM_ANDAMENTO","dataRealizacao":"2026-11-03T14:30:00"
 * }
 */
public record SessaoResponse(
        Long atendimentoId,
        Integer numeroSessao,
        Integer qtdAutorizada,
        Integer qtdRealizada,
        Integer saldo,
        String statusProcedimento,
        LocalDateTime dataRealizacao
) {
}