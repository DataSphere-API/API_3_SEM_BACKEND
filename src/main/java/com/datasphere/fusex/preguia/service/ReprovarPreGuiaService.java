package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.StatusPreGuia;
import com.datasphere.fusex.preguia.dto.AprovacaoResponse;
import com.datasphere.fusex.preguia.service.IdentificarAprovadorService.Aprovador;
import com.datasphere.fusex.preguia.service.IdentificarAprovadorService.Papel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ReprovarPreGuiaService {

    private final BuscarPreGuiaPorIdService buscarPreGuiaPorIdService;
    private final IdentificarAprovadorService identificarAprovadorService;
    private final RegistrarStatusPreGuiaService registrarStatusPreGuiaService;

    public ReprovarPreGuiaService(BuscarPreGuiaPorIdService buscarPreGuiaPorIdService,
                                  IdentificarAprovadorService identificarAprovadorService,
                                  RegistrarStatusPreGuiaService registrarStatusPreGuiaService) {
        this.buscarPreGuiaPorIdService = buscarPreGuiaPorIdService;
        this.identificarAprovadorService = identificarAprovadorService;
        this.registrarStatusPreGuiaService = registrarStatusPreGuiaService;
    }

    @Transactional
    public AprovacaoResponse reprovar(Long id, String precCpAprovador, String motivo) {
        PreGuiaModel preGuia = buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(id);
        Aprovador aprovador = identificarAprovadorService.identificar(precCpAprovador, preGuia);

        if (motivo == null || motivo.isBlank()) {
            throw new BusinessException(ErrorCode.MOTIVO_OBRIGATORIO);
        }
        if (preGuia.getStatus() != StatusPreGuia.VALIDADA) {
            throw new BusinessException(ErrorCode.PRE_GUIA_NAO_VALIDADA);
        }

        preGuia.setMotivoReprovacao(motivo.trim());
        preGuia.setDataReprovacao(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        if (aprovador.papel() == Papel.CHEFE) {
            preGuia.setChefeId(aprovador.id());
        }
        registrarStatusPreGuiaService.atualizarStatus(preGuia, StatusPreGuia.REPROVADA, "reprovacao-pre-guia");

        return new AprovacaoResponse(
                preGuia.getId(),
                preGuia.getStatus(),
                preGuia.getChefeId(),
                preGuia.getDataAprovacaoMedico(),
                preGuia.getDataAprovacaoChefe(),
                preGuia.getMotivoReprovacao());
    }
}