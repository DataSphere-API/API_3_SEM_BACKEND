package com.datasphere.fusex.espelhoItem.dto;

import com.datasphere.fusex.espelhoItem.EspelhoItemModel;

import java.math.BigDecimal;

public record EspelhoItemResponse(Integer id, String beneficiarioId, String descricao, BigDecimal valor) {

    public static EspelhoItemResponse from(EspelhoItemModel espelhoItemModel){
        return new EspelhoItemResponse(espelhoItemModel.getId(),
                espelhoItemModel.getBeneficiarioId(),
                espelhoItemModel.getDescricao(),
                espelhoItemModel.getValor());
    }

}
