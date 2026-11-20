package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import com.datasphere.fusex.guiaproc.StatusProcedimento;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.service.RegistrarMudancaStatusService;
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
        procedimento.setStatus(StatusProcedimento.REALIZADA.name());
        procedimento.setDataValidacao(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        GuiaProcModel salvo = guiaProcRepository.save (procedimento);

        registrarMudancaStatusService.executar(EntidadeTipo.GUIA_PROC, salvo.getId(),
                statusAnterior, StatusProcedimento.REALIZADA.name(), "validacao-qrcode");
        return salvo;
    }
}