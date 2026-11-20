package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.DecisaoEspelhoResponse;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.service.RegistrarMudancaStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReenviarEspelhoService {

    private final EspelhoRepository espelhoRepository;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public ReenviarEspelhoService(EspelhoRepository espelhoRepository,
                                  RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.espelhoRepository = espelhoRepository;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    @Transactional
    public DecisaoEspelhoResponse reenviarEspelho(Integer id) {
        EspelhoModel espelho = espelhoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ESPELHO_NAO_ENCONTRADO));

        if (espelho.getStatus() != StatusEspelho.REPROVADO) {
            throw new BusinessException(ErrorCode.ESPELHO_NAO_REPROVADO);
        }

        String statusAnterior = espelho.getStatus().name();
        espelho.setStatus(StatusEspelho.EM_ABERTO);
        espelho.setMotivoReprovacao(null);
        espelho.setAuditorId(null);
        espelhoRepository.save(espelho);

        registrarMudancaStatusService.executar(
                EntidadeTipo.ESPELHO,
                espelho.getId().longValue(),
                statusAnterior,
                StatusEspelho.EM_ABERTO.name(),
                "reenvio-espelho"
        );

        return new DecisaoEspelhoResponse(
                espelho.getId(),
                espelho.getStatus(),
                espelho.getMotivoReprovacao(),
                espelho.getAuditorId()
        );
    }
}
