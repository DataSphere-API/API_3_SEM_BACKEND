package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ValidarAtendimentoRealizadoService {

    private final GuiaProcRepository guiaProcRepository;

    public ValidarAtendimentoRealizadoService(GuiaProcRepository guiaProcRepository) {
        this.guiaProcRepository = guiaProcRepository;
    }

    public GuiaProcModel validarAtendimentoRealizado(Long atendimentoId) {
        Optional<GuiaProcModel> atendimentoOptional = guiaProcRepository.findById(atendimentoId);

        if (atendimentoOptional.isEmpty()) {
            return null;
        }

        GuiaProcModel atendimento = atendimentoOptional.get();
        if (!"Realizada".equals(atendimento.getStatus())) {
            return null;
        }

        return atendimento;
    }
}