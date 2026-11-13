package com.datasphere.fusex.ocs.service;

import com.datasphere.fusex.ocs.OcsRepository;
import com.datasphere.fusex.ocs.dto.OcsResponse;
import com.datasphere.fusex.ocs.dto.OcsListaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

@Service
public class ListarOcsService {

    private final OcsRepository repository;

    public ListarOcsService(OcsRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public OcsListaResponse listar() {
        return new OcsListaResponse(repository.findAll(Sort.by("nome").ascending()).stream().map(OcsResponse::de).toList());
    }
}
