package com.datasphere.fusex.encaminhamento.service;

import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.StatusFatura;
import org.springframework.stereotype.Service;

@Service
public class ValidarFaturaEncaminhavelService {

    public boolean validarFaturaEncaminhavel(FaturaModel fatura) {
        return fatura.getStatus() == StatusFatura.ATIVA;
    }
}