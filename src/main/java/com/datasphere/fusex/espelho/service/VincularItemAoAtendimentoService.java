package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import org.springframework.stereotype.Service;

@Service
public class VincularItemAoAtendimentoService {

    public EspelhoItemModel vincularItemAoAtendimento(EspelhoItemModel item, Long atendimentoId) {
        item.setAtendimentoId(atendimentoId);
        return item;
    }
}