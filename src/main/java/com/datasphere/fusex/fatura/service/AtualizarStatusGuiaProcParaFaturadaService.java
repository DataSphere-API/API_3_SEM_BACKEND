package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.guiaProc.StatusProcedimento;
import org.springframework.stereotype.Service;

@Service
public class AtualizarStatusGuiaProcParaFaturadaService {

    private final GuiaProcRepository guiaProcRepository;

    public AtualizarStatusGuiaProcParaFaturadaService(GuiaProcRepository guiaProcRepository) {
        this.guiaProcRepository = guiaProcRepository;
    }

    public GuiaProcModel atualizarStatusGuiaProcParaFaturada(GuiaProcModel procedimento) {
        procedimento.setStatus(StatusProcedimento.FATURADA);
        return guiaProcRepository.save(procedimento);
    }
}