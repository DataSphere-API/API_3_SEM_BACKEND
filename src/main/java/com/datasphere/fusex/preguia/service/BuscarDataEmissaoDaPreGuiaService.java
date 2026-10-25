package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.preguia.PreGuiaModel;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class BuscarDataEmissaoDaPreGuiaService {

    private final GuiaRepository guiaRepository;

    public BuscarDataEmissaoDaPreGuiaService(GuiaRepository guiaRepository) {
        this.guiaRepository = guiaRepository;
    }

    public LocalDate buscarDataEmissao(PreGuiaModel preGuia) {
        if (preGuia.getGuiaId() == null) {
            return null;
        }
        GuiaModel guia = guiaRepository.findById(preGuia.getGuiaId()).orElse(null);
        if (guia == null) {
            return null;
        }
        return guia.getDataEmissao();
    }
}