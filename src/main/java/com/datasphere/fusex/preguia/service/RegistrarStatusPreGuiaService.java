package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaRepository;
import com.datasphere.fusex.preguia.StatusPreGuia;
import org.springframework.stereotype.Service;

@Service
public class RegistrarStatusPreGuiaService {

    private final PreGuiaRepository preGuiaRepository;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public RegistrarStatusPreGuiaService(PreGuiaRepository preGuiaRepository,
                                         RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.preGuiaRepository = preGuiaRepository;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    public PreGuiaModel atualizarStatus(PreGuiaModel preGuia, StatusPreGuia novoStatus, String origem) {
        String statusAnterior = preGuia.getStatus().name();
        preGuia.setStatus(novoStatus);
        PreGuiaModel salva = preGuiaRepository.save(preGuia);
        registrarMudancaStatusService.executar(EntidadeTipo.PRE_GUIA, salva.getId(),
                statusAnterior, novoStatus.name(), origem);
        return salva;
    }
}