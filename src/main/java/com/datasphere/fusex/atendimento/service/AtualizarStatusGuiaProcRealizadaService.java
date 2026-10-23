package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AtualizarStatusGuiaProcRealizadaService {

    private GuiaProcRepository guiaProcRepository;
    private RegistrarMudancaStatusService registrarMudancaStatusService;

    public AtualizarStatusGuiaProcRealizadaService(GuiaProcRepository guiaProcRepository,
                                                   RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.guiaProcRepository = guiaProcRepository;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    public GuiaProcModel atualizarStatusGuiaProcRealizada (GuiaProcModel procedimento) {
        String statusAnterior = procedimento.getStatus();
        procedimento.setStatus("Realizada");
        procedimento.setDataValidacao(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        GuiaProcModel salvo = guiaProcRepository.save (procedimento);

        registrarMudancaStatusService.executar(EntidadeTipo.GUIA_PROC, salvo.getGuiaId(),
                statusAnterior, "Realizada", "validacao-qrcode");
        return salvo;
    }
}