package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.StatusEspelho;
import org.springframework.stereotype.Service;

@Service
public class ValidarEspelhoFaturavelService {

    public boolean validarEspelhoFaturavel(EspelhoModel espelho) {
        return espelho.getStatus() == StatusEspelho.EM_ABERTO;
    }
}