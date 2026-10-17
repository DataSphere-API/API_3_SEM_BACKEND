package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaRepository;
import com.datasphere.fusex.preguia.StatusPreGuia;
import com.datasphere.fusex.preguia.request.AprovacaoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AtualizarStatusPreGuiaService {

    private final PreGuiaRepository preGuiaRepository;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public AtualizarStatusPreGuiaService(PreGuiaRepository preGuiaRepository,
                                         RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.preGuiaRepository = preGuiaRepository;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    @Transactional
    public AprovacaoResponse atualizarStatusPreGuia(PreGuiaModel preGuia,
                                                    StatusPreGuia novoStatus,
                                                    String motivo) {
        String statusAnterior = preGuia.getStatus().name();

        preGuia.setStatus(novoStatus);
        preGuia.setDataDecisao(LocalDateTime.now().withNano(0));
        preGuia.setMotivoReprovacao(novoStatus == StatusPreGuia.REPROVADA ? motivo : null);
        preGuiaRepository.save(preGuia);

        String origem = novoStatus == StatusPreGuia.APROVADA
                ? "aprovacao-pre-guia"
                : "reprovacao-pre-guia";
        registrarMudancaStatusService.executar(EntidadeTipo.PRE_GUIA, preGuia.getId(),
                statusAnterior, novoStatus.name(), origem);

        return new AprovacaoResponse(
                preGuia.getId(),
                preGuia.getStatus(),
                preGuia.getChefeId(),
                preGuia.getDataDecisao(),
                preGuia.getMotivoReprovacao());
    }
}