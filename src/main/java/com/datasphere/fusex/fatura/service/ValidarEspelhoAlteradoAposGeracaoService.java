package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.fatura.FaturaModel;
import org.springframework.stereotype.Service;

@Service
public class ValidarEspelhoAlteradoAposGeracaoService {

    public boolean validarEspelhoAlteradoAposGeracao(FaturaModel fatura) {
        EspelhoModel espelho = fatura.getEspelhoModel();

        if (espelho.getDataAtualizacao() == null) {
            return false;
        }

        return espelho.getDataAtualizacao().isAfter(fatura.getDataGeracao());
    }
}