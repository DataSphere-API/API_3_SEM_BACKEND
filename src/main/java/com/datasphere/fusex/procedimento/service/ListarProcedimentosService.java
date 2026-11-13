package com.datasphere.fusex.procedimento.service;

import com.datasphere.fusex.procedimento.ProcedimentoRepository;
import com.datasphere.fusex.procedimento.dto.ProcedimentoResponse;
import com.datasphere.fusex.procedimento.dto.ProcedimentosResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

@Service
public class ListarProcedimentosService {

    private final ProcedimentoRepository repository;

    public ListarProcedimentosService(ProcedimentoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ProcedimentosResponse listar() {
        return new ProcedimentosResponse(repository.findAll(Sort.by("nome").ascending()).stream().map(ProcedimentoResponse::de).toList());
    }
}
