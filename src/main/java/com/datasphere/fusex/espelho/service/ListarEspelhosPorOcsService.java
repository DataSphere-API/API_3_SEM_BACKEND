package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarEspelhosPorOcsService {

    private final EspelhoRepository espelhoRepository;

    public ListarEspelhosPorOcsService(EspelhoRepository espelhoRepository) {
        this.espelhoRepository = espelhoRepository;
    }

    public List<EspelhoModel> listarEspelhosPorOcs(String cnpj) {
        return espelhoRepository.findByOcsId(cnpj);
    }
}