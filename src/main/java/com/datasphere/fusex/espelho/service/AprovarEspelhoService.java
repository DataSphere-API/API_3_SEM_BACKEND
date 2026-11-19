package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.DecisaoEspelhoResponse;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.fatura.FaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.fatura.service.AlimentarFaturaNaAprovacaoService;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.service.RegistrarMudancaStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AprovarEspelhoService {

    // Fatura do mês que já saiu da auditoria não recebe mais espelho.
    private static final List<StatusFatura> FATURA_FECHADA =
            List.of(StatusFatura.ENCAMINHADA_APROVACAO, StatusFatura.APROVADA, StatusFatura.LIQUIDADA);

    private final EspelhoRepository espelhoRepository;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;
    private final FaturaRepository faturaRepository;
    private final AlimentarFaturaNaAprovacaoService alimentarFaturaNaAprovacaoService;

    public AprovarEspelhoService(EspelhoRepository espelhoRepository,
                                 RegistrarMudancaStatusService registrarMudancaStatusService,
                                 FaturaRepository faturaRepository,
                                 AlimentarFaturaNaAprovacaoService alimentarFaturaNaAprovacaoService) {
        this.espelhoRepository = espelhoRepository;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
        this.faturaRepository = faturaRepository;
        this.alimentarFaturaNaAprovacaoService = alimentarFaturaNaAprovacaoService;
    }

    // Aprovar = o espelho entra na fatura do mês da OCS e fica FATURADO.
    @Transactional
    public DecisaoEspelhoResponse aprovarEspelho(Integer id, Integer auditorId) {
        EspelhoModel espelho = espelhoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ESPELHO_NAO_ENCONTRADO));
        if (espelho.getStatus() != StatusEspelho.EM_ABERTO) {
            throw new BusinessException(ErrorCode.ESPELHO_JA_AUDITADO);
        }
        String competencia = espelho.getDataInicio().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        if (!faturaRepository.findByOcsIdAndCompetenciaAndStatusIn(espelho.getOcsId(), competencia, FATURA_FECHADA).isEmpty()) {
            throw new BusinessException(ErrorCode.FATURA_JA_ENCAMINHADA);
        }
        String statusAnterior = espelho.getStatus().name();
        espelho.setStatus(StatusEspelho.APROVADO);
        espelho.setAuditorId(auditorId);
        espelhoRepository.save(espelho);
        registrarMudancaStatusService.executar(EntidadeTipo.ESPELHO, espelho.getId().longValue(),
                statusAnterior, StatusEspelho.APROVADO.name(), "auditoria-aprovacao");

        alimentarFaturaNaAprovacaoService.processarAprovacaoEspelho(espelho);

        return new DecisaoEspelhoResponse(espelho.getId(), espelho.getStatus(), espelho.getMotivoReprovacao(),
                espelho.getAuditorId());
    }
}
