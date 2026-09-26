package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.FaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarFaturasPorStatusService {

    private final FaturaRepository faturaRepository;

    public ListarFaturasPorStatusService(FaturaRepository faturaRepository) {
        this.faturaRepository = faturaRepository;
    }

    public List<FaturaModel> listarFaturasPorStatus(StatusFatura status) {
        if (status == null) {
            return faturaRepository.findAll();
        }
        return faturaRepository.findByStatus(status);
    }
}