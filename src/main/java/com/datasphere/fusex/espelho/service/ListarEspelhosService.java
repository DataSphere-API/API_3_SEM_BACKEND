package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarEspelhosService {

    private final EspelhoRepository espelhoRepository;
    private final EnriquecerListaEspelhosService enriquecerListaEspelhosService;

    public ListarEspelhosService(EspelhoRepository espelhoRepository,
                                 EnriquecerListaEspelhosService enriquecerListaEspelhosService) {
        this.espelhoRepository = espelhoRepository;
        this.enriquecerListaEspelhosService = enriquecerListaEspelhosService;
    }

    public List<EspelhoModel> listarEspelhos() {
        List<EspelhoModel> espelhos = espelhoRepository.findAll();

        enriquecerListaEspelhosService.enriquecer(espelhos);

        return espelhos;
    }
}