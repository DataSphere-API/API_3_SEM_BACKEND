package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guia.StatusGuia;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class RecalcularStatusDaGuiaService {

    private static final Set<String> STATUS_REALIZADO = Set.of("REALIZADA", "REALIZADO", "FATURADA");

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

        boolean todosRealizados = !procedimentos.isEmpty() && procedimentos.stream()
                .allMatch(p -> p.getStatus() != null && STATUS_REALIZADO.contains(p.getStatus().toUpperCase()));

        String novoStatus = todosRealizados ? "REALIZADA" : "PARCIALMENTE_REALIZADA";
        String statusAnterior = guia.getStatus();

        if (!todosRealizados) {
            return statusAnterior; // a dev não tem estado parcial: segue EMITIDA
        }

        if (!novoStatus.equals(statusAnterior)) {
            guiaRepository.atualizarStatus(guiaId, novoStatus);
            registrarMudancaStatusService.executar(EntidadeTipo.GUIA, guiaId,
                    statusAnterior, novoStatus, "validacao-qrcode");
        }
        return novoStatus;
    }
}