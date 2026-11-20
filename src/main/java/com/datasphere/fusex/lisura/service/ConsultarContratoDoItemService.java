package com.datasphere.fusex.lisura.service;

import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import org.springframework.stereotype.Service;

@Service
public class ConsultarContratoDoItemService {

    private final GuiaProcRepository guiaProcRepository;

    public ConsultarContratoDoItemService(GuiaProcRepository guiaProcRepository) {
        this.guiaProcRepository = guiaProcRepository;
    }

    public GuiaProcModel consultarAtendimentoDoItem(EspelhoItemModel item) {
        return guiaProcRepository.findById(item.getAtendimentoId()).orElse(null);
    }
}