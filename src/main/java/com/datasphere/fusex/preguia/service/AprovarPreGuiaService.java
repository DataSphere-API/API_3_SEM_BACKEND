package com.datasphere.fusex.preguia.service;

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
public class AprovarPreGuiaService {

    private final BuscarPreGuiaPorIdService buscarPreGuiaPorIdService;
    private final IdentificarAprovadorService identificarAprovadorService;
    private final ValidarPreGuiaAguardandoDecisaoService validarPreGuiaAguardandoDecisaoService;
    private final AssinarDecisaoDaPreGuiaService assinarDecisaoDaPreGuiaService;
    private final RegistrarStatusPreGuiaService registrarStatusPreGuiaService;
    private final MontarAssinaturaService montarAssinaturaService;

    public AprovarPreGuiaService(BuscarPreGuiaPorIdService buscarPreGuiaPorIdService,
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

    // Basta uma aprovação (médico responsável ou chefe do FUSEx) para a pré-guia ficar APROVADA.
    @Transactional
    public AprovacaoResponse aprovar(Long id) {
        PreGuiaModel preGuia = buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(id);
        Aprovador aprovador = identificarAprovadorService.identificar(preGuia);
        validarPreGuiaAguardandoDecisaoService.validar(preGuia);

        LocalDateTime agora = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String origem;
        if (aprovador.papel() == Papel.MEDICO) {
            preGuia.setDataAprovacaoMedico(agora);
            origem = "aprovacao-medico";
        } else {
            preGuia.setChefeId(aprovador.id());
            preGuia.setDataAprovacaoChefe(agora);
            origem = "aprovacao-chefe";
        }
        assinarDecisaoDaPreGuiaService.assinar(preGuia, aprovador, agora);
        registrarStatusPreGuiaService.atualizarStatus(preGuia, StatusPreGuia.APROVADA, origem);

        return new AprovacaoResponse(
                preGuia.getId(),
                preGuia.getStatus(),
                preGuia.getChefeId(),
                preGuia.getDataAprovacaoMedico(),
                preGuia.getDataAprovacaoChefe(),
                null,
                montarAssinaturaService.montar(preGuia));
    }
}
