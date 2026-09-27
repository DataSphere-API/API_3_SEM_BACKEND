package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.springframework.stereotype.Service;

@Service
public class AtualizarStatusGuiaProcParaFaturadaService {

    private final GuiaProcRepository guiaProcRepository;

    public AtualizarStatusGuiaProcParaFaturadaService(GuiaProcRepository guiaProcRepository) {
        this.guiaProcRepository = guiaProcRepository;
    }

    public GuiaProcModel atualizarStatusGuiaProcParaFaturada(GuiaProcModel procedimento) {
        procedimento.setStatus("Faturada");
        return guiaProcRepository.save(procedimento);
    }
}