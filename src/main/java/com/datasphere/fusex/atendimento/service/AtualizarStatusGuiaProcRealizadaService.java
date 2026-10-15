package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.guiaProc.StatusProcedimento;
import org.springframework.stereotype.Service;

@Service
public class AtualizarStatusGuiaProcRealizadaService {

    private GuiaProcRepository guiaProcRepository;

    public AtualizarStatusGuiaProcRealizadaService(GuiaProcRepository guiaProcRepository) {
        this.guiaProcRepository = guiaProcRepository;
    }

    public GuiaProcModel atualizarStatusGuiaProcRealizada (GuiaProcModel procedimento) {
        procedimento.setStatus(StatusProcedimento.REALIZADA);
        return guiaProcRepository.save (procedimento);
    }
}
