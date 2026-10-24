package com.datasphere.fusex.preguia.dto;

public record PendenciaResponse(CodigoPendencia codigo, Long atendimentoId, String mensagem) {

    public enum CodigoPendencia {
        BENEFICIARIO_INVALIDO,
        MEDICO_SEM_CRM,
        PROCEDIMENTO_SEM_CONTRATO,
        SESSOES_INVALIDAS,
        VALOR_TOTAL_DIVERGENTE,
        PRE_GUIA_SEM_PROCEDIMENTOS
    }
}