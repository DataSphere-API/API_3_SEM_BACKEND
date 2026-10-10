package com.datasphere.fusex.espelho.request;

import java.util.List;

public record EspelhoRequest(
        Long atendimentoId,
        String ocsId,
        List<ItemEspelhoRequest> itens
) {}