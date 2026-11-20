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
    private final ValidarPreGuiaAguardandoDecisaoService validarPreGuiaAguardandoDecisaoService;
    private final AssinarDecisaoDaPreGuiaService assinarDecisaoDaPreGuiaService;
    private final RegistrarStatusPreGuiaService registrarStatusPreGuiaService;
    private final MontarAssinaturaService montarAssinaturaService;

    public ReprovarPreGuiaService(BuscarPreGuiaPorIdService buscarPreGuiaPorIdService,
                                  IdentificarAprovadorService identificarAprovadorService,
                                  ValidarPreGuiaAguardandoDecisaoService validarPreGuiaAguardandoDecisaoService,
                                  AssinarDecisaoDaPreGuiaService assinarDecisaoDaPreGuiaService,
                                  RegistrarStatusPreGuiaService registrarStatusPreGuiaService,
                                  MontarAssinaturaService montarAssinaturaService) {
        this.buscarPreGuiaPorIdService = buscarPreGuiaPorIdService;
        this.identificarAprovadorService = identificarAprovadorService;
        this.validarPreGuiaAguardandoDecisaoService = validarPreGuiaAguardandoDecisaoService;
        this.assinarDecisaoDaPreGuiaService = assinarDecisaoDaPreGuiaService;
        this.registrarStatusPreGuiaService = registrarStatusPreGuiaService;
        this.montarAssinaturaService = montarAssinaturaService;
    }

    @Transactional
    public AprovacaoResponse reprovar(Long id, String motivo) {
        PreGuiaModel preGuia = buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(id);
        Aprovador aprovador = identificarAprovadorService.identificar(preGuia);

        if (motivo == null || motivo.isBlank()) {
            throw new BusinessException(ErrorCode.MOTIVO_OBRIGATORIO);
        }
        validarPreGuiaAguardandoDecisaoService.validar(preGuia);

        LocalDateTime agora = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        preGuia.setMotivoReprovacao(motivo.trim());
        preGuia.setDataReprovacao(agora);
        if (aprovador.papel() == Papel.CHEFE) {
            preGuia.setChefeId(aprovador.id());
        }
        assinarDecisaoDaPreGuiaService.assinar(preGuia, aprovador, agora);
        registrarStatusPreGuiaService.atualizarStatus(preGuia, StatusPreGuia.REPROVADA, "reprovacao-pre-guia");

        return new AprovacaoResponse(
                preGuia.getId(),
                preGuia.getStatus(),
                preGuia.getChefeId(),
                preGuia.getDataAprovacaoMedico(),
                preGuia.getDataAprovacaoChefe(),
                preGuia.getMotivoReprovacao(),
                montarAssinaturaService.montar(preGuia));
    }
}
