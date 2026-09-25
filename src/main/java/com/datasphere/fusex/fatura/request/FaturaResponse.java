package com.datasphere.fusex.fatura.request;

import com.datasphere.fusex.fatura.ItemFaturaModel;

import java.util.List;

public record FaturaResponse(Long id, Long atendimentoId, Integer espelhoId, List<ItemFaturaModel> itens) {}