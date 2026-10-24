package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaRepository;
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
    private final PreGuiaRepository preGuiaRepository;
    private final RegistrarStatusPreGuiaService registrarStatusPreGuiaService;

    public AprovarPreGuiaService(BuscarPreGuiaPorIdService buscarPreGuiaPorIdService,
                                 IdentificarAprovadorService identificarAprovadorService,
                                 PreGuiaRepository preGuiaRepository,
                                 RegistrarStatusPreGuiaService registrarStatusPreGuiaService) {
        this.buscarPreGuiaPorIdService = buscarPreGuiaPorIdService;
        this.identificarAprovadorService = identificarAprovadorService;
        this.preGuiaRepository = preGuiaRepository;
        this.registrarStatusPreGuiaService = registrarStatusPreGuiaService;
    }

    @Transactional
    public AprovacaoResponse aprovar(Long id, String precCpAprovador) {
        PreGuiaModel preGuia = buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(id);
        Aprovador aprovador = identificarAprovadorService.identificar(precCpAprovador, preGuia);

        if (preGuia.getStatus() != StatusPreGuia.VALIDADA) {
            throw new BusinessException(ErrorCode.PRE_GUIA_NAO_VALIDADA);
        }

        LocalDateTime agora = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        if (aprovador.papel() == Papel.MEDICO) {
            aprovarComoMedico(preGuia, agora);
        } else {
            aprovarComoChefe(preGuia, aprovador.id(), agora);
        }

        return new AprovacaoResponse(
                preGuia.getId(),
                preGuia.getStatus(),
                preGuia.getChefeId(),
                preGuia.getDataAprovacaoMedico(),
                preGuia.getDataAprovacaoChefe(),
                null);
    }

    private void aprovarComoMedico(PreGuiaModel preGuia, LocalDateTime agora) {
        if (preGuia.getDataAprovacaoMedico() != null) {
            throw new BusinessException(ErrorCode.APROVACAO_JA_REGISTRADA);
        }
        preGuia.setDataAprovacaoMedico(agora);
        preGuiaRepository.save(preGuia);
    }

    private void aprovarComoChefe(PreGuiaModel preGuia, Integer chefeId, LocalDateTime agora) {
        if (preGuia.getDataAprovacaoMedico() == null) {
            throw new BusinessException(ErrorCode.APROVACAO_MEDICO_PENDENTE);
        }
        preGuia.setChefeId(chefeId);
        preGuia.setDataAprovacaoChefe(agora);
        registrarStatusPreGuiaService.atualizarStatus(preGuia, StatusPreGuia.APROVADA, "aprovacao-chefe");
    }
}