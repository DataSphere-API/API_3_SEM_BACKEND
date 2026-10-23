package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaRepository;
import org.springframework.stereotype.Service;

@Service
public class BuscarPreGuiaPorIdService {

    private final PreGuiaRepository preGuiaRepository;

    public BuscarPreGuiaPorIdService(PreGuiaRepository preGuiaRepository) {
        this.preGuiaRepository = preGuiaRepository;
    }

    public PreGuiaModel buscarPreGuiaPorId(Long id) {
        return preGuiaRepository.findById(id).orElse(null);
    }
}