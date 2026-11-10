package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
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
        return preGuiaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRE_GUIA_NAO_ENCONTRADA));
    }

    public PreGuiaModel buscarPreGuiaParaAtualizar(Long id) {
        return preGuiaRepository.findByIdParaAtualizar(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRE_GUIA_NAO_ENCONTRADA));
    }
}