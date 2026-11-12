package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.FaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransicionarStatusDaFaturaService {

    private final FaturaRepository faturaRepository;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public TransicionarStatusDaFaturaService(FaturaRepository faturaRepository,
                                             RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.faturaRepository = faturaRepository;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    @Transactional
    public void aprovarFatura(Long id) {
        transicionar(id, StatusFatura.ENCAMINHADA_APROVACAO, StatusFatura.APROVADA, "aprovacao-da-fatura");
    }

    @Transactional
    public void liquidarFatura(Long id) {
        transicionar(id, StatusFatura.APROVADA, StatusFatura.LIQUIDADA, "liquidacao-da-fatura");
    }

    private void transicionar(Long id, StatusFatura esperado, StatusFatura destino, String origem) {
        FaturaModel fatura = faturaRepository.findByIdParaAlterarStatus(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FATURA_NAO_ENCONTRADA));
        if (fatura.getStatus() != esperado) {
            throw new BusinessException(ErrorCode.FATURA_TRANSICAO_INVALIDA);
        }

        fatura.setStatus(destino);
        registrarMudancaStatusService.executar(EntidadeTipo.FATURA, id, esperado.name(), destino.name(), origem);
    }
}
