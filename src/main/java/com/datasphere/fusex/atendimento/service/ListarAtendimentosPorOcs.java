package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarAtendimentosPorOcs {

    GuiaProcRepository guiaProcRepository;

    public ListarAtendimentosPorOcs(GuiaProcRepository guiaProcRepository) {
        this.guiaProcRepository = guiaProcRepository;
    }

    public List<GuiaProcModel> listarAtendimentosPorOcs(String cnpj) {
        return guiaProcRepository.findByContratoModel_OcsModel_Cnpj(cnpj);
    }

}
