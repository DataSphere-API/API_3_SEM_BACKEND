package com.datasphere.fusex.lisura.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.lisura.dto.LisuraItemResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AvaliarAutorizacaoDoItemService {

    public LisuraItemResponse avaliarAutorizacaoDoItem(EspelhoItemModel item, ContratoModel contrato,
                                                       String ocsId, LocalDate dataValidade) {
        if (contrato == null) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    null, false, "Procedimento sem contrato");
        }

        String ocsDoContrato = contrato.getOcsModel().getCnpj();

        if (!ocsDoContrato.equals(ocsId)) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    null, false, "Procedimento sem contrato com a OCS da fatura");
        }

        if (dataValidade != null && LocalDate.now().isAfter(dataValidade)) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    contrato.getValor(), false, "Guia vencida: apresentada após o prazo de validade");
        }

        int comparacao = item.getValor().compareTo(contrato.getValor());

        if (comparacao > 0) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    contrato.getValor(), false, "Valor apresentado acima do valor contratado");
        }

        if (comparacao < 0) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    contrato.getValor(), false, "Valor apresentado abaixo do valor contratado");
        }

        return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                contrato.getValor(), true, null);
    }
}