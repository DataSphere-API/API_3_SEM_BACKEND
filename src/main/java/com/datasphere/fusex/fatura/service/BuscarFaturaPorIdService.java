package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.FaturaRepository;
import org.springframework.stereotype.Service;

@Service
public class BuscarFaturaPorIdService {

    private final FaturaRepository faturaRepository;

    public BuscarFaturaPorIdService(FaturaRepository faturaRepository) {
        this.faturaRepository = faturaRepository;
    }

    public FaturaModel buscarFaturaPorId(Long id) {
        return faturaRepository.findById(id).orElse(null);
    }
}