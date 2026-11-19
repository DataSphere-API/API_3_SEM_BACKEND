package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.DecisaoEspelhoResponse;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.FaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.service.RegistrarMudancaStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class AprovarEspelhoService {

    private final EspelhoRepository espelhoRepository;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;
    private final FaturaRepository faturaRepository;

    public AprovarEspelhoService(EspelhoRepository espelhoRepository,
                                 RegistrarMudancaStatusService registrarMudancaStatusService,
                                 FaturaRepository faturaRepository) {
        this.espelhoRepository = espelhoRepository;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
        this.faturaRepository = faturaRepository;
    }

    @Transactional
    public DecisaoEspelhoResponse aprovarEspelho(Integer id, Integer auditorId) {
        EspelhoModel espelho = espelhoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ESPELHO_NAO_ENCONTRADO));

        if (espelho.getStatus() != StatusEspelho.EM_ABERTO) {
            throw new BusinessException(ErrorCode.ESPELHO_JA_AUDITADO);
        }

        if (espelho.getOcsId() != null && espelho.getDataInicio() != null) {
            String competencia = espelho.getDataInicio().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Optional<FaturaModel> faturaEncaminhada = faturaRepository.findByOcsIdAndCompetenciaAndStatus(
                    espelho.getOcsId(), competencia, StatusFatura.ENCAMINHADA_APROVACAO);
            if (faturaEncaminhada.isPresent()) {
                throw new BusinessException(ErrorCode.FATURA_JA_ENCAMINHADA);
            }
        }

        String statusAnterior = espelho.getStatus().name();
        espelho.setStatus(StatusEspelho.APROVADO);
        espelho.setAuditorId(auditorId);
        espelhoRepository.save(espelho);

        registrarMudancaStatusService.executar(
                EntidadeTipo.ESPELHO,
                espelho.getId().longValue(),
                statusAnterior,
                StatusEspelho.APROVADO.name(),
                "auditoria-aprovacao"
        );

        // Ponto de integração com US002-8:
        // AlimentarFaturaNaAprovacaoService existente força espelho.status = FATURADO.
        // A integração com a fatura mensal deve ser ajustada para preservar o status APROVADO.

        return new DecisaoEspelhoResponse(
                espelho.getId(),
                espelho.getStatus(),
                espelho.getMotivoReprovacao(),
                espelho.getAuditorId()
        );
    }
}
