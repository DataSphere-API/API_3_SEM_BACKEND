package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.dto.EspelhoResumoResponse;
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

    public List<EspelhoResumoResponse> listarEspelhos() {

        List<EspelhoModel> espelhos = espelhoRepository.findAll();

        return enriquecerListaEspelhosService.enriquecer(espelhos);
    }
}