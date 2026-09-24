package com.datasphere.fusex.historicoStatus;

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
        HistoricoStatusModel evento = new HistoricoStatusModel(
                tipo, entidadeId, statusAnterior, statusNovo, origem);
        repository.save(evento);
    }

    public List<HistoricoStatusModel> listarHistoricoPorEntidade(EntidadeTipo tipo, Long entidadeId) {
        return repository.findByEntidadeTipoAndEntidadeIdOrderByDataAsc(tipo, entidadeId);
    }

    public List<HistoricoStatusModel> ordenarEventosPorData(List<HistoricoStatusModel> eventos) {
        return eventos.stream()
                .sorted((a, b) -> a.getData().compareTo(b.getData()))
                .toList();
    }
}