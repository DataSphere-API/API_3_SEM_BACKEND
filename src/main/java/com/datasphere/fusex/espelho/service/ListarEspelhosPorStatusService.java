package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarEspelhosPorStatusService {

    private final EspelhoRepository espelhoRepository;

    public ListarEspelhosPorStatusService(EspelhoRepository espelhoRepository) {
        this.espelhoRepository = espelhoRepository;
    }

    public List<EspelhoModel> listarEspelhosPorStatus(StatusEspelho status) {
        return espelhoRepository.findByStatus(status);
    }
}