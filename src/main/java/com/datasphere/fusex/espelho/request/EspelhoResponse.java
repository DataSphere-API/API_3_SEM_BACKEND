package com.datasphere.fusex.espelho.request;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.request.EspelhoItemResponse;

import java.time.LocalDateTime;
import java.util.List;

public record EspelhoResponse(
        Integer id,
        Long atendimentoId,
        StatusEspelho status,
        LocalDateTime dataAtualizacao,
        List<EspelhoItemResponse> itens
) {

   public static EspelhoResponse from(EspelhoModel espelhoModel, List<EspelhoItemModel> itens){
       return new EspelhoResponse(espelhoModel.getId(),
               espelhoModel.getAtendimentoId(),
               espelhoModel.getStatus(),
               espelhoModel.getDataAtualizacao(),
               itens.stream().map(EspelhoItemResponse::from).toList());
   }

}