package com.datasphare.fusex.service;

import com.datasphare.fusex.domain.HistoricoStatus;
import com.datasphare.fusex.domain.enums.EntidadeTipo;
import com.datasphare.fusex.repository.HistoricoStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoricoStatusService {

    private final HistoricoStatusRepository repository;

    public HistoricoStatusService(HistoricoStatusRepository repository) {
        this.repository = repository;
    }

    public void registrarMudancaStatus(EntidadeTipo tipo, Long entidadeId,
                                       String statusAnterior, String statusNovo,
                                       String origem) {
        HistoricoStatus evento = new HistoricoStatus(
                tipo, entidadeId, statusAnterior, statusNovo, origem);
        repository.save(evento);
    }

    public List<HistoricoStatus> listarHistoricoPorEntidade(EntidadeTipo tipo, Long entidadeId) {
        return repository.findByEntidadeTipoAndEntidadeIdOrderByDataAsc(tipo, entidadeId);
    }

    public List<HistoricoStatus> ordenarEventosPorData(List<HistoricoStatus> eventos) {
        return eventos.stream()
                .sorted((a, b) -> a.getData().compareTo(b.getData()))
                .toList();
    }
}