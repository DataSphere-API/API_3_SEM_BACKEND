package com.datasphere.fusex.espelho.request;

import com.datasphere.fusex.espelhoItem.EspelhoItemModel;

import java.util.List;

public record EspelhoResponse(
        Integer id,
        Long atendimentoId,
        List<EspelhoItemModel> itens
) {}