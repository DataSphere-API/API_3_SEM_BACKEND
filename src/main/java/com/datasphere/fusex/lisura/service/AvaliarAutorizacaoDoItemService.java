package com.datasphere.fusex.lisura.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.fatura.service.CalcularDivergenciaValorService;
import com.datasphere.fusex.lisura.dto.LisuraItemResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AvaliarAutorizacaoDoItemService {

    private final CalcularDivergenciaValorService calcularDivergenciaValorService;

    public AvaliarAutorizacaoDoItemService(CalcularDivergenciaValorService calcularDivergenciaValorService) {
        this.calcularDivergenciaValorService = calcularDivergenciaValorService;
    }

    public LisuraItemResponse avaliarAutorizacaoDoItem(EspelhoItemModel item, ContratoModel contrato,
                                                       String ocsId, LocalDate dataValidade,
                                                       LocalDate dataAtendimento) {
        if (contrato == null) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    null, false, "Procedimento sem contrato");
        }

        String ocsDoContrato = contrato.getOcsModel().getCnpj();

        if (!ocsDoContrato.equals(ocsId)) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    null, false, "Procedimento sem contrato com a OCS da fatura");
        }

        if (dataValidade != null && dataAtendimento == null) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    contrato.getValor(), false, "Atendimento sem data registrada");
        }

        if (dataValidade != null && dataAtendimento.isAfter(dataValidade)) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    contrato.getValor(), false, "Guia vencida: atendimento após o prazo de validade");
        }

        if (calcularDivergenciaValorService
                .calcularDivergenciaValor(item.getValor(), contrato.getValor()).signum() > 0) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    contrato.getValor(), false, "Valor apresentado acima do valor contratado");
        }

        return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                contrato.getValor(), true, null);
    }
}