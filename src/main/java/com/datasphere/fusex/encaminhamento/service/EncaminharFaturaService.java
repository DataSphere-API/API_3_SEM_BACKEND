package com.datasphere.fusex.encaminhamento.service;

import com.datasphere.fusex.encaminhamento.dto.EncaminharFaturaResponse;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.FaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EncaminharFaturaService {

    private final FaturaRepository faturaRepository;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public EncaminharFaturaService(FaturaRepository faturaRepository,
                                   RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.faturaRepository = faturaRepository;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    @Transactional
    public EncaminharFaturaResponse encaminharFatura(FaturaModel fatura) {
        String statusAnterior = fatura.getStatus().name();

        fatura.setStatus(StatusFatura.ENCAMINHADA_APROVACAO);
        fatura.setDataEncaminhamento(LocalDateTime.now().withNano(0));
        faturaRepository.save(fatura);

        registrarMudancaStatusService.executar(EntidadeTipo.FATURA, fatura.getId(),
                statusAnterior, StatusFatura.ENCAMINHADA_APROVACAO.name(), "encaminhamento-para-aprovacao");

        return new EncaminharFaturaResponse(fatura.getId(), fatura.getStatus(), fatura.getDataEncaminhamento());
    }
}