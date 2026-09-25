package com.datasphere.fusex.historicoStatus.service;

import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.HistoricoStatusModel;
import com.datasphere.fusex.historicoStatus.HistoricoStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarHistoricoPorEntidadeService {

    private final HistoricoStatusRepository repository;

    public ListarHistoricoPorEntidadeService(HistoricoStatusRepository repository) {
        this.repository = repository;
    }

    public List<HistoricoStatusModel> executar(EntidadeTipo tipo, Long entidadeId) {
        return repository.findByEntidadeTipoAndEntidadeIdOrderByDataAsc(tipo, entidadeId);
    }
}