package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import org.springframework.stereotype.Service;

@Service
public class BuscarEspelhoPorIdService {

    private final EspelhoRepository espelhoRepository;

    public BuscarEspelhoPorIdService(EspelhoRepository espelhoRepository) {
        this.espelhoRepository = espelhoRepository;
    }

    public EspelhoModel buscarEspelhoPorId(Integer id) {
        return espelhoRepository.findById(id).orElse(null);
    }
}