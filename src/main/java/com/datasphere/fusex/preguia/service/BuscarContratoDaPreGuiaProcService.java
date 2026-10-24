package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.contrato.ContratoRepository;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import org.springframework.stereotype.Service;

@Service
public class BuscarContratoDaPreGuiaProcService {

    private final ContratoRepository contratoRepository;

    public BuscarContratoDaPreGuiaProcService(ContratoRepository contratoRepository) {
        this.contratoRepository = contratoRepository;
    }

    public ContratoModel buscarContrato(PreGuiaProcModel procedimento) {
        return contratoRepository
                .findFirstByProcedimentoModel_IdAndOcsModel_CnpjOrderByIdAsc(
                        procedimento.getProcedimentoModel().getId(),
                        procedimento.getOcsModel().getCnpj())
                .orElse(null);
    }
}