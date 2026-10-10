package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.espelho.EspelhoRepository;
import org.springframework.stereotype.Service;

@Service
public class ValidarEspelhoNaoDuplicadoPorAtendimentoService {

    private final EspelhoRepository espelhoRepository;

    public ValidarEspelhoNaoDuplicadoPorAtendimentoService(EspelhoRepository espelhoRepository) {
        this.espelhoRepository = espelhoRepository;
    }

    public boolean validarEspelhoNaoDuplicadoPorAtendimento(Long atendimentoId) {
        return espelhoRepository.findByAtendimentoId(atendimentoId).isEmpty();
    }
}