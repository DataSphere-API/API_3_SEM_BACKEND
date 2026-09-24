package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;

import java.util.List;

public class ListarAtendimentosPorOcs {

    GuiaProcRepository guiaProcRepository;

    public ListarAtendimentosPorOcs(GuiaProcRepository guiaProcRepository) {
        this.guiaProcRepository = guiaProcRepository;
    }

    public List<GuiaProcModel> listarAtendimentosPorOcs(String cnpj) {
        return guiaProcRepository.findAll();
    }

}
