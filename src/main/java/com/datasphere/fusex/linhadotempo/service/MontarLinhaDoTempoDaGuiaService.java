package com.datasphere.fusex.linhadotempo.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.linhadotempo.dto.EventoLinhaDoTempoResponse;
import com.datasphere.fusex.linhadotempo.dto.LinhaDoTempoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MontarLinhaDoTempoDaGuiaService {

    private final GuiaRepository guiaRepository;
    private final GuiaProcRepository guiaProcRepository;
    private final BuscarEventosDaPreGuiaService buscarEventosDaPreGuiaService;
    private final BuscarEventosDaGuiaEAtendimentosService buscarEventosDaGuiaEAtendimentosService;
    private final BuscarEventosDoFaturamentoService buscarEventosDoFaturamentoService;

    public MontarLinhaDoTempoDaGuiaService(GuiaRepository guiaRepository,
                                           GuiaProcRepository guiaProcRepository,
                                           BuscarEventosDaPreGuiaService buscarEventosDaPreGuiaService,
                                           BuscarEventosDaGuiaEAtendimentosService buscarEventosDaGuiaEAtendimentosService,
                                           BuscarEventosDoFaturamentoService buscarEventosDoFaturamentoService) {
        this.guiaRepository = guiaRepository;
        this.guiaProcRepository = guiaProcRepository;
        this.buscarEventosDaPreGuiaService = buscarEventosDaPreGuiaService;
        this.buscarEventosDaGuiaEAtendimentosService = buscarEventosDaGuiaEAtendimentosService;
        this.buscarEventosDoFaturamentoService = buscarEventosDoFaturamentoService;
    }

    @Transactional(readOnly = true)
    public LinhaDoTempoResponse montar(Long guiaId) {
        GuiaModel guia = guiaRepository.findById(guiaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GUIA_NAO_ENCONTRADA));

        List<GuiaProcModel> procedimentos = guiaProcRepository.findByGuiaModel_Id(guiaId);
        List<Long> atendimentoIds = new ArrayList<>();
        for (GuiaProcModel p : procedimentos) {
            atendimentoIds.add(p.getGuiaId());
        }

        List<EventoLinhaDoTempoResponse> eventos = new ArrayList<>();
        eventos.addAll(buscarEventosDaPreGuiaService.buscar(guiaId));
        eventos.addAll(buscarEventosDaGuiaEAtendimentosService.buscar(guiaId, procedimentos));
        eventos.addAll(buscarEventosDoFaturamentoService.buscar(atendimentoIds));

        eventos.sort(Comparator
                .comparing(EventoLinhaDoTempoResponse::data)
                .thenComparing(EventoLinhaDoTempoResponse::etapa));

        return new LinhaDoTempoResponse(guia.getId(), guia.getStatus(), guia.getDataEmissao(), eventos);
    }
}