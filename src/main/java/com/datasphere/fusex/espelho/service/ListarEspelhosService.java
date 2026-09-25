package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarEspelhosService {

    private final EspelhoRepository espelhoRepository;

    public ListarEspelhosService(EspelhoRepository espelhoRepository) {
        this.espelhoRepository = espelhoRepository;
    }

    public List<EspelhoModel> listarEspelhos() {
        return espelhoRepository.findAll();
    }
}