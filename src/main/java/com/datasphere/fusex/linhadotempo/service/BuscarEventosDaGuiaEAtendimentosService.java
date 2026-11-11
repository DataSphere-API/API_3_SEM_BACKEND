package com.datasphere.fusex.linhadotempo.service;

import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.HistoricoStatusModel;
import com.datasphere.fusex.historicoStatus.HistoricoStatusRepository;
import com.datasphere.fusex.linhadotempo.dto.EventoLinhaDoTempoResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BuscarEventosDaGuiaEAtendimentosService {

    private final HistoricoStatusRepository historicoStatusRepository;
    private final ConverterHistoricoEmEventoService converterHistoricoEmEventoService;

    public BuscarEventosDaGuiaEAtendimentosService(HistoricoStatusRepository historicoStatusRepository,
                                                   ConverterHistoricoEmEventoService converterHistoricoEmEventoService) {
        this.historicoStatusRepository = historicoStatusRepository;
        this.converterHistoricoEmEventoService = converterHistoricoEmEventoService;
    }

    public List<EventoLinhaDoTempoResponse> buscar(Long guiaId, List<GuiaProcModel> procedimentos) {
        List<EventoLinhaDoTempoResponse> eventos = new ArrayList<>();

        for (HistoricoStatusModel h : historicoStatusRepository
                .findByEntidadeTipoAndEntidadeIdOrderByDataAsc(EntidadeTipo.GUIA, guiaId)) {
            eventos.add(converterHistoricoEmEventoService.converter(h, null));
        }

        if (procedimentos.isEmpty()) {
            return eventos;
        }

        Map<Long, String> nomePorProcedimento = new HashMap<>();
        for (GuiaProcModel p : procedimentos) {
            nomePorProcedimento.put(p.getGuiaId(), p.getContratoModel().getProcedimentoModel().getNome());
        }

        List<HistoricoStatusModel> historicoDosProcedimentos = historicoStatusRepository
                .findByEntidadeTipoAndEntidadeIdIn(EntidadeTipo.GUIA_PROC, nomePorProcedimento.keySet());
        for (HistoricoStatusModel h : historicoDosProcedimentos) {
            eventos.add(converterHistoricoEmEventoService.converter(h, nomePorProcedimento.get(h.getEntidadeId())));
        }
        return eventos;
    }
}