package com.datasphere.fusex.contrato.service;

import com.datasphere.fusex.contrato.ContratoRepository;
import com.datasphere.fusex.contrato.dto.ContratoResponse;
import com.datasphere.fusex.contrato.dto.ContratosResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarContratosService {

    private final ContratoRepository repository;

    public ListarContratosService(ContratoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ContratosResponse listar() {
        return new ContratosResponse(repository.findAllByOrderByIdAsc().stream().map(ContratoResponse::de).toList());
    }
}
