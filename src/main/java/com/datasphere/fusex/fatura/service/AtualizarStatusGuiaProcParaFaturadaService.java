package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import com.datasphere.fusex.guiaproc.StatusProcedimento;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.service.RegistrarMudancaStatusService;
import org.springframework.stereotype.Service;

@Service
public class AtualizarStatusGuiaProcParaFaturadaService {

    private final GuiaProcRepository guiaProcRepository;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public AtualizarStatusGuiaProcParaFaturadaService(GuiaProcRepository guiaProcRepository,
                                                      RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.guiaProcRepository = guiaProcRepository;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    public GuiaProcModel atualizarStatusGuiaProcParaFaturada(GuiaProcModel procedimento) {
        String statusAnterior = procedimento.getStatus();
        procedimento.setStatus(StatusProcedimento.FATURADA.name());
        GuiaProcModel salvo = guiaProcRepository.save(procedimento);
        registrarMudancaStatusService.executar(EntidadeTipo.GUIA_PROC, salvo.getId(),
                statusAnterior, StatusProcedimento.FATURADA.name(), "inclusao-na-fatura");
        return salvo;
    }
}
