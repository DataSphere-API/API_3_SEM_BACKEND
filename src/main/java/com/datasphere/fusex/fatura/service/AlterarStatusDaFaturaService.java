package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.FaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.fatura.dto.StatusDaFaturaResponse;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.service.RegistrarMudancaStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlterarStatusDaFaturaService {

    private final FaturaRepository faturaRepository;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public AlterarStatusDaFaturaService(FaturaRepository faturaRepository,
                                        RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.faturaRepository = faturaRepository;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    // Fluxo: ENCAMINHADA_APROVACAO -> APROVADA -> LIQUIDADA, sem pular nem voltar.
    @Transactional
    public StatusDaFaturaResponse aprovar(Long id) {
        return alterar(id, StatusFatura.ENCAMINHADA_APROVACAO, StatusFatura.APROVADA, "aprovacao-da-fatura");
    }

    @Transactional
    public StatusDaFaturaResponse liquidar(Long id) {
        return alterar(id, StatusFatura.APROVADA, StatusFatura.LIQUIDADA, "liquidacao-da-fatura");
    }

    private StatusDaFaturaResponse alterar(Long id, StatusFatura esperado, StatusFatura novo, String origem) {
        FaturaModel fatura = faturaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FATURA_NAO_ENCONTRADA));
        if (fatura.getStatus() != esperado) {
            throw new BusinessException(ErrorCode.FATURA_TRANSICAO_INVALIDA);
        }

        fatura.setStatus(novo);
        faturaRepository.save(fatura);
        registrarMudancaStatusService.executar(EntidadeTipo.FATURA, id, esperado.name(), novo.name(), origem);
        return new StatusDaFaturaResponse(id, novo);
    }
}
