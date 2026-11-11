package com.datasphere.fusex.linhadotempo.service;

import com.datasphere.fusex.historicoStatus.HistoricoStatusModel;
import com.datasphere.fusex.linhadotempo.dto.EventoLinhaDoTempoResponse;
import org.springframework.stereotype.Service;

@Service
public class ConverterHistoricoEmEventoService {

    private final ClassificarEtapaDoEventoService classificarEtapaDoEventoService;
    private final DescreverEventoService descreverEventoService;

    public ConverterHistoricoEmEventoService(ClassificarEtapaDoEventoService classificarEtapaDoEventoService,
                                             DescreverEventoService descreverEventoService) {
        this.classificarEtapaDoEventoService = classificarEtapaDoEventoService;
        this.descreverEventoService = descreverEventoService;
    }

    public EventoLinhaDoTempoResponse converter(HistoricoStatusModel historico, String complemento) {
        String descricao = descreverEventoService.descrever(
                historico.getStatusAnterior(), historico.getStatusNovo(), historico.getOrigem());
        if (complemento != null && !complemento.isBlank()) {
            descricao = complemento + " — " + descricao;
        }

        return new EventoLinhaDoTempoResponse(
                classificarEtapaDoEventoService.classificar(historico.getEntidadeTipo(), historico.getStatusNovo()),
                historico.getEntidadeTipo(),
                historico.getEntidadeId(),
                historico.getStatusAnterior(),
                historico.getStatusNovo(),
                descricao,
                historico.getData(),
                historico.getOrigem());
    }
}