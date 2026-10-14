package com.datasphere.fusex.fatura.dto;

import com.datasphere.fusex.fatura.ItemFaturaModel;

import java.math.BigDecimal;

public record ItemFaturaResponse(Integer id,
                                 Long faturaId,
                                 Integer espelhoItemId,
                                 BigDecimal valorApresentado,
                                 BigDecimal valorContratado,
                                 BigDecimal divergenciaValor) {

    public static ItemFaturaResponse from(ItemFaturaModel itemFaturaModel){
        return new ItemFaturaResponse(itemFaturaModel.getId(),
                itemFaturaModel.getFaturaId(),
                itemFaturaModel.getEspelhoItemId(),
                itemFaturaModel.getValorApresentado(),
                itemFaturaModel.getValorContratado(),
                itemFaturaModel.getDivergenciaValor());
    }

}
