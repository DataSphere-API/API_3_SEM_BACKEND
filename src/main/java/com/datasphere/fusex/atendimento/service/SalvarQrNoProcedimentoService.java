package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalvarQrNoProcedimentoService {

    private final GuiaProcRepository guiaProcRepository;

    public SalvarQrNoProcedimentoService(GuiaProcRepository guiaProcRepository) {
        this.guiaProcRepository = guiaProcRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void salvarQrNoProcedimento(GuiaProcModel procedimento, String codigo) {
        procedimento.setQr(codigo);
        guiaProcRepository.saveAndFlush(procedimento);
    }
}
