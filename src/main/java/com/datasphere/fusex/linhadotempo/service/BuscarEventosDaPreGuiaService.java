package com.datasphere.fusex.linhadotempo.service;

import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.HistoricoStatusModel;
import com.datasphere.fusex.historicoStatus.HistoricoStatusRepository;
import com.datasphere.fusex.linhadotempo.dto.EtapaLinhaDoTempo;
import com.datasphere.fusex.linhadotempo.dto.EventoLinhaDoTempoResponse;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BuscarEventosDaPreGuiaService {

    private final PreGuiaRepository preGuiaRepository;
    private final HistoricoStatusRepository historicoStatusRepository;
    private final ConverterHistoricoEmEventoService converterHistoricoEmEventoService;

    public BuscarEventosDaPreGuiaService(PreGuiaRepository preGuiaRepository,
                                         HistoricoStatusRepository historicoStatusRepository,
                                         ConverterHistoricoEmEventoService converterHistoricoEmEventoService) {
        this.preGuiaRepository = preGuiaRepository;
        this.historicoStatusRepository = historicoStatusRepository;
        this.converterHistoricoEmEventoService = converterHistoricoEmEventoService;
    }

    public List<EventoLinhaDoTempoResponse> buscar(Long guiaId) {
        List<EventoLinhaDoTempoResponse> eventos = new ArrayList<>();

        for (PreGuiaModel preGuia : preGuiaRepository.findByGuiaIdOrderByIdAsc(guiaId)) {
            eventos.add(eventoDeSolicitacao(preGuia));

            if (preGuia.getDataAprovacaoMedico() != null) {
                eventos.add(eventoDeAprovacaoDoMedico(preGuia));
            }

            List<HistoricoStatusModel> historico = historicoStatusRepository
                    .findByEntidadeTipoAndEntidadeIdOrderByDataAsc(EntidadeTipo.PRE_GUIA, preGuia.getId());
            for (HistoricoStatusModel h : historico) {
                eventos.add(converterHistoricoEmEventoService.converter(h, null));
            }
        }
        return eventos;
    }

    private EventoLinhaDoTempoResponse eventoDeSolicitacao(PreGuiaModel preGuia) {
        return new EventoLinhaDoTempoResponse(
                EtapaLinhaDoTempo.SOLICITACAO,
                EntidadeTipo.PRE_GUIA,
                preGuia.getId(),
                null,
                "PRE_GUIA",
                "Pré-guia criada (solicitação de exame)",
                preGuia.getDataCriacao().atStartOfDay(),
                "solicitacao-de-exame");
    }

    private EventoLinhaDoTempoResponse eventoDeAprovacaoDoMedico(PreGuiaModel preGuia) {
        return new EventoLinhaDoTempoResponse(
                EtapaLinhaDoTempo.APROVACAO,
                EntidadeTipo.PRE_GUIA,
                preGuia.getId(),
                null,
                "APROVADA_PELO_MEDICO",
                "Pré-guia aprovada pelo médico responsável",
                preGuia.getDataAprovacaoMedico(),
                "aprovacao-medico");
    }
}