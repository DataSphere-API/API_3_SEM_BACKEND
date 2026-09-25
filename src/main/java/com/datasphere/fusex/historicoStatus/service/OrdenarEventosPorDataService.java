package com.datasphere.fusex.historicoStatus.service;

import com.datasphere.fusex.historicoStatus.HistoricoStatusModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdenarEventosPorDataService {

    public List<HistoricoStatusModel> executar(List<HistoricoStatusModel> eventos) {
        return eventos.stream()
                .sorted((a, b) -> a.getData().compareTo(b.getData()))
                .toList();
    }
}