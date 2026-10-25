package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guia.StatusGuia;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.guiaProc.StatusProcedimento;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecalcularStatusDaGuiaService {

    private final GuiaProcRepository guiaProcRepository;
    private final GuiaRepository guiaRepository;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public RecalcularStatusDaGuiaService(GuiaProcRepository guiaProcRepository,
                                         GuiaRepository guiaRepository,
                                         RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.guiaProcRepository = guiaProcRepository;
        this.guiaRepository = guiaRepository;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    public String recalcularStatusDaGuia(Long guiaId) {
        GuiaModel guia = guiaRepository.findByIdParaAtualizar(guiaId)
                .orElseThrow(() -> new IllegalStateException("Guia não encontrada: " + guiaId));

        List<GuiaProcModel> procedimentos = guiaProcRepository.findByGuiaModel_Id(guiaId);

        String statusAnterior = guia.getStatus();
        String statusFinal = StatusGuia.FINALIZADA.name();

        if (!todosRealizados(procedimentos)) {
            return statusAnterior;
        }

        if (!statusFinal.equals(statusAnterior)) {
            guiaRepository.atualizarStatus(guiaId, statusFinal);
            registrarMudancaStatusService.executar(EntidadeTipo.GUIA, guiaId,
                    statusAnterior, statusFinal, "validacao-qrcode");
        }
        return statusFinal;
    }

    private boolean todosRealizados(List<GuiaProcModel> procedimentos) {
        if (procedimentos.isEmpty()) {
            return false;
        }
        for (GuiaProcModel p : procedimentos) {
            boolean realizada = StatusProcedimento.REALIZADA.name().equalsIgnoreCase(p.getStatus());
            boolean faturada = StatusProcedimento.FATURADA.name().equalsIgnoreCase(p.getStatus());
            if (!realizada && !faturada) {
                return false;
            }
        }
        return true;
    }
}