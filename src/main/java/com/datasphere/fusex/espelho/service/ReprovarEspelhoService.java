package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.request.DecisaoEspelhoResponse;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReprovarEspelhoService {

    private final EspelhoRepository espelhoRepository;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public ReprovarEspelhoService(EspelhoRepository espelhoRepository,
                                  RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.espelhoRepository = espelhoRepository;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    @Transactional
    public DecisaoEspelhoResponse reprovarEspelho(Integer id, Integer auditorId, String motivo) {
        EspelhoModel espelho = espelhoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ESPELHO_NAO_ENCONTRADO));

        if (motivo == null || motivo.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.MOTIVO_OBRIGATORIO);
        }

        if (espelho.getStatus() != StatusEspelho.EM_ABERTO) {
            throw new BusinessException(ErrorCode.ESPELHO_JA_AUDITADO);
        }

        String statusAnterior = espelho.getStatus().name();
        espelho.setStatus(StatusEspelho.REPROVADO);
        espelho.setMotivoReprovacao(motivo.trim());
        espelho.setAuditorId(auditorId);
        espelhoRepository.save(espelho);

        registrarMudancaStatusService.executar(
                EntidadeTipo.ESPELHO,
                espelho.getId().longValue(),
                statusAnterior,
                StatusEspelho.REPROVADO.name(),
                "auditoria-reprovacao"
        );

        return new DecisaoEspelhoResponse(
                espelho.getId(),
                espelho.getStatus(),
                espelho.getMotivoReprovacao(),
                espelho.getAuditorId()
        );
    }
}
