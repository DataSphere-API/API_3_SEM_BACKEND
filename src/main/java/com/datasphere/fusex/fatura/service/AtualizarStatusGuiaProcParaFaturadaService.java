package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import com.datasphere.fusex.guiaproc.StatusProcedimento;
import org.springframework.stereotype.Service;

@Service
public class AtualizarStatusGuiaProcParaFaturadaService {

    private final GuiaProcRepository guiaProcRepository;

    public AtualizarStatusGuiaProcParaFaturadaService(GuiaProcRepository guiaProcRepository) {
        this.guiaProcRepository = guiaProcRepository;
    }

    public GuiaProcModel atualizarStatusGuiaProcParaFaturada(GuiaProcModel procedimento) {
        procedimento.setStatus(StatusProcedimento.FATURADA.name());
        return guiaProcRepository.save(procedimento);
    }
}