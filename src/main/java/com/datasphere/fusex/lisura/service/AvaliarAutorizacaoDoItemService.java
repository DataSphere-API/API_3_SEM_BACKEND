package com.datasphere.fusex.lisura.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.lisura.dto.LisuraItemResponse;
import org.springframework.stereotype.Service;

@Service
public class AvaliarAutorizacaoDoItemService {

    public LisuraItemResponse avaliarAutorizacaoDoItem(EspelhoItemModel item, ContratoModel contrato, String ocsId) {
        if (contrato == null) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    null, false, "Procedimento sem contrato");
        }

        String ocsDoContrato = contrato.getOcsModel().getCnpj();

        if (!ocsDoContrato.equals(ocsId)) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    null, false, "Procedimento sem contrato com a OCS da fatura");
        }

        if (item.getValor().compareTo(contrato.getValor()) > 0) {
            return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                    contrato.getValor(), false, "Valor apresentado acima do valor contratado");
        }

        return new LisuraItemResponse(item.getId(), item.getDescricao(), item.getValor(),
                contrato.getValor(), true, null);
    }
}