package com.datasphere.fusex.linhadotempo.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemRepository;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.FaturaRepository;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.HistoricoStatusModel;
import com.datasphere.fusex.historicoStatus.HistoricoStatusRepository;
import com.datasphere.fusex.linhadotempo.dto.EventoLinhaDoTempoResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class BuscarEventosDoFaturamentoService {

    private final EspelhoRepository espelhoRepository;
    private final EspelhoItemRepository espelhoItemRepository;
    private final FaturaRepository faturaRepository;
    private final HistoricoStatusRepository historicoStatusRepository;
    private final ConverterHistoricoEmEventoService converterHistoricoEmEventoService;

    public BuscarEventosDoFaturamentoService(EspelhoRepository espelhoRepository,
                                             EspelhoItemRepository espelhoItemRepository,
                                             FaturaRepository faturaRepository,
                                             HistoricoStatusRepository historicoStatusRepository,
                                             ConverterHistoricoEmEventoService converterHistoricoEmEventoService) {
        this.espelhoRepository = espelhoRepository;
        this.espelhoItemRepository = espelhoItemRepository;
        this.faturaRepository = faturaRepository;
        this.historicoStatusRepository = historicoStatusRepository;
        this.converterHistoricoEmEventoService = converterHistoricoEmEventoService;
    }

    public List<EventoLinhaDoTempoResponse> buscar(List<Long> atendimentoIds) {
        List<EventoLinhaDoTempoResponse> eventos = new ArrayList<>();
        if (atendimentoIds.isEmpty()) {
            return eventos;
        }

        Set<Integer> espelhoIds = new LinkedHashSet<>();
        for (EspelhoModel espelho : espelhoRepository.findByAtendimentoIdIn(atendimentoIds)) {
            espelhoIds.add(espelho.getId());
        }
        for (EspelhoItemModel item : espelhoItemRepository.findByAtendimentoIdIn(atendimentoIds)) {
            espelhoIds.add(item.getEspelhoId());
        }
        if (espelhoIds.isEmpty()) {
            return eventos;
        }

        eventos.addAll(eventosDosEspelhos(espelhoIds));
        eventos.addAll(eventosDasFaturas(espelhoIds));
        return eventos;
    }

    private List<EventoLinhaDoTempoResponse> eventosDosEspelhos(Set<Integer> espelhoIds) {
        Set<Long> ids = new LinkedHashSet<>();
        for (Integer id : espelhoIds) {
            ids.add(id.longValue());
        }

        List<EventoLinhaDoTempoResponse> eventos = new ArrayList<>();
        for (HistoricoStatusModel h : historicoStatusRepository
                .findByEntidadeTipoAndEntidadeIdIn(EntidadeTipo.ESPELHO, ids)) {
            eventos.add(converterHistoricoEmEventoService.converter(h, "Espelho #" + h.getEntidadeId()));
        }
        return eventos;
    }

    private List<EventoLinhaDoTempoResponse> eventosDasFaturas(Set<Integer> espelhoIds) {
        Set<Long> ids = new LinkedHashSet<>();
        for (FaturaModel fatura : faturaRepository.findByEspelhoModel_IdIn(espelhoIds)) {
            ids.add(fatura.getId());
        }

        List<EventoLinhaDoTempoResponse> eventos = new ArrayList<>();
        if (ids.isEmpty()) {
            return eventos;
        }
        for (HistoricoStatusModel h : historicoStatusRepository
                .findByEntidadeTipoAndEntidadeIdIn(EntidadeTipo.FATURA, ids)) {
            eventos.add(converterHistoricoEmEventoService.converter(h, "Fatura #" + h.getEntidadeId()));
        }
        return eventos;
    }
}