package com.datasphere.fusex.espelhoitem.dto;

import com.datasphere.fusex.espelhoitem.EspelhoItemModel;

import java.math.BigDecimal;

// valorContratado = contrato por sessão x sessões previstas na guia; null quando não há contrato.
public record EspelhoItemResponse(Integer id, Long atendimentoId, String beneficiarioId, String descricao,
                                  BigDecimal valor, BigDecimal valorContratado) {

    public static EspelhoItemResponse from(EspelhoItemModel item, BigDecimal valorContratado) {
        return new EspelhoItemResponse(item.getId(), item.getAtendimentoId(), item.getBeneficiarioId(),
                item.getDescricao(), item.getValor(), valorContratado);
    }
}
