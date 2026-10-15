package com.datasphere.fusex.fatura.dto;

import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.ItemFaturaModel;
import com.datasphere.fusex.fatura.StatusFatura;

import java.time.LocalDateTime;
import java.util.List;

public record FaturaResponse(
        Long id,
        Integer atendimentoId,
        Integer espelhoId,
        String ocsId,
        String ocsNome,
        List<ItemFaturaModel> itens,
        StatusFatura status,
        LocalDateTime dataGeracao,
        Long faturaOrigemId) {

    public static FaturaResponse from(
            FaturaModel fatura,
            List<ItemFaturaModel> itens) {

        return new FaturaResponse(
                fatura.getId(),
                fatura.getGuiaProcModel().getId(),
                fatura.getEspelhoModel().getId(),
                fatura.getEspelhoModel().getOcsId(),
                null,
                itens,
                fatura.getStatus(),
                fatura.getDataGeracao(),
                fatura.getFaturaOrigemId()
        );
    }
}