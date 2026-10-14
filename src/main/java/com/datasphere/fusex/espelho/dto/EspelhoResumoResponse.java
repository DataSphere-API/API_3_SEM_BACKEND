package com.datasphere.fusex.espelho.dto;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.StatusEspelho;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EspelhoResumoResponse(Integer id,
                                    Long atendimentoId,
                                    String ocsId,
                                    LocalDate dataInicio,
                                    LocalDate dataFim,
                                    StatusEspelho status,
                                    LocalDateTime dataAtualizacao,
                                    String ocsNome,
                                    Integer quantidadeItens,
                                    BigDecimal valorTotal) {

    public static EspelhoResumoResponse from(EspelhoModel espelhoModel, String ocsNome, Integer quantidadeItens, BigDecimal valorTotal){

        return new EspelhoResumoResponse(espelhoModel.getId(),
                espelhoModel.getAtendimentoId(),
                espelhoModel.getOcsId(),
                espelhoModel.getDataInicio(),
                espelhoModel.getDataFim(),
                espelhoModel.getStatus(),
                espelhoModel.getDataAtualizacao(),
                ocsNome,
                quantidadeItens,
                valorTotal);
    }
}
