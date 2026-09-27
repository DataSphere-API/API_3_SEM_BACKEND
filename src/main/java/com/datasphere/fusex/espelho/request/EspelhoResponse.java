package com.datasphere.fusex.espelho.request;

import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;

import java.util.List;

public record EspelhoResponse(
        Integer id,
        Long atendimentoId,
        StatusEspelho status,
        List<EspelhoItemModel> itens
) {}