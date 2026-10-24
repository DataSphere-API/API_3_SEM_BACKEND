package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guia.StatusGuia;
import com.datasphere.fusex.guia.dto.request.ExpiracaoResponse;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.guiaProc.StatusProcedimento;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ExpirarGuiasService {

    private static final String ORIGEM = "expiracao-de-guias";

    private final GuiaRepository guiaRepository;
    private final GuiaProcRepository guiaProcRepository;
    private final ValidadeGuiaService validadeGuiaService;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public ExpirarGuiasService(GuiaRepository guiaRepository,
                               GuiaProcRepository guiaProcRepository,
                               ValidadeGuiaService validadeGuiaService,
                               RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.guiaRepository = guiaRepository;
        this.guiaProcRepository = guiaProcRepository;
        this.validadeGuiaService = validadeGuiaService;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    @Transactional
    public ExpiracaoResponse expirarGuiasVencidas() {
        List<GuiaModel> vencidas = buscarGuiasVencidas();

        int procedimentosExpirados = 0;
        for (GuiaModel guia : vencidas) {
            procedimentosExpirados += expirarProcedimentosNaoRealizados(guia.getId());
            atualizarStatusDaGuia(guia);
        }

        return new ExpiracaoResponse(vencidas.size(), procedimentosExpirados,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }

    List<GuiaModel> buscarGuiasVencidas() {
        LocalDate limite = validadeGuiaService.calcularLimiteDeEmissaoVencida();
        return guiaRepository.findByStatusAndDataEmissaoBefore(StatusGuia.EMITIDA.name(), limite);
    }

    int expirarProcedimentosNaoRealizados(Long guiaId) {
        int expirados = 0;
        for (GuiaProcModel procedimento : guiaProcRepository.findByGuiaModel_Id(guiaId)) {
            if (!jaFoiRealizado(procedimento)) {
                String statusAnterior = procedimento.getStatus();
                procedimento.setStatus(StatusProcedimento.EXPIRADA.name());
                guiaProcRepository.save(procedimento);
                registrarMudancaStatusService.executar(EntidadeTipo.GUIA_PROC, procedimento.getGuiaId(),
                        statusAnterior, StatusProcedimento.EXPIRADA.name(), ORIGEM);
                expirados++;
            }
        }
        return expirados;
    }

    boolean jaFoiRealizado(GuiaProcModel procedimento) {
        String status = procedimento.getStatus();
        return StatusProcedimento.REALIZADA.name().equalsIgnoreCase(status)
                || StatusProcedimento.FATURADA.name().equalsIgnoreCase(status)
                || StatusProcedimento.EXPIRADA.name().equalsIgnoreCase(status);
    }

    void atualizarStatusDaGuia(GuiaModel guia) {
        String statusAnterior = guia.getStatus();
        guia.setStatus(StatusGuia.EXPIRADA.name());
        guiaRepository.save(guia);
        registrarMudancaStatusService.executar(EntidadeTipo.GUIA, guia.getId(),
                statusAnterior, StatusGuia.EXPIRADA.name(), ORIGEM);
    }
}