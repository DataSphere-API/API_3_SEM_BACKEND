package com.datasphere.fusex.encaminhamento.service;

import com.datasphere.fusex.lisura.dto.LisuraItemResponse;
import com.datasphere.fusex.lisura.dto.LisuraResponse;
import org.springframework.stereotype.Service;

@Service
public class ValidarAuditoriaConcluidaService {

    public boolean validarAuditoriaConcluida(LisuraResponse lisura) {
        if (lisura.itens().isEmpty()) {
            return false;
        }

        for (LisuraItemResponse item : lisura.itens()) {
            if (!item.autorizado()) {
                return false;
            }
        }

        return true;
    }
}