package com.datasphere.fusex.lisura.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.springframework.stereotype.Service;

@Service
public class ConsultarContratoDoItemService {

    private final GuiaProcRepository guiaProcRepository;

    public ConsultarContratoDoItemService(GuiaProcRepository guiaProcRepository) {
        this.guiaProcRepository = guiaProcRepository;
    }

    public ContratoModel consultarContratoDoItem(EspelhoItemModel item) {
        GuiaProcModel guiaProc = guiaProcRepository.findById(item.getAtendimentoId()).orElse(null);

        if (guiaProc == null) {
            return null;
        }

        return guiaProc.getContratoModel();
    }
}