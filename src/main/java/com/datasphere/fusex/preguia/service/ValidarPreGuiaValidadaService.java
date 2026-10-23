package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.StatusPreGuia;
import org.springframework.stereotype.Service;

@Service
public class ValidarPreGuiaValidadaService {

    public boolean validarPreGuiaValidada(PreGuiaModel preGuia) {
        return preGuia.getStatus() == StatusPreGuia.VALIDADA;
    }
}