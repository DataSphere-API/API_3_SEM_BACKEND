package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.FaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.service.RegistrarMudancaStatusService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AlterarStatusDaFaturaServiceTest {

    private final FaturaRepository faturaRepository = mock(FaturaRepository.class);
    private final RegistrarMudancaStatusService historico = mock(RegistrarMudancaStatusService.class);
    private final AlterarStatusDaFaturaService service = new AlterarStatusDaFaturaService(faturaRepository, historico);

    private FaturaModel fatura(StatusFatura status) {
        FaturaModel fatura = new FaturaModel();
        fatura.setId(1L);
        fatura.setStatus(status);
        when(faturaRepository.findById(1L)).thenReturn(Optional.of(fatura));
        return fatura;
    }

    @Test
    void aprovaFaturaEncaminhadaEGravaHistorico() {
        FaturaModel fatura = fatura(StatusFatura.ENCAMINHADA_APROVACAO);

        assertEquals(StatusFatura.APROVADA, service.aprovar(1L).status());
        assertEquals(StatusFatura.APROVADA, fatura.getStatus());
        verify(historico).executar(EntidadeTipo.FATURA, 1L, "ENCAMINHADA_APROVACAO", "APROVADA", "aprovacao-da-fatura");
    }

    @Test
    void liquidaFaturaAprovada() {
        fatura(StatusFatura.APROVADA);

        assertEquals(StatusFatura.LIQUIDADA, service.liquidar(1L).status());
    }

    @Test
    void naoPulaNemVoltaStatus() {
        fatura(StatusFatura.ATIVA);

        BusinessException erro = assertThrows(BusinessException.class, () -> service.liquidar(1L));
        assertEquals(ErrorCode.FATURA_TRANSICAO_INVALIDA, erro.getErrorCode());
        verifyNoInteractions(historico);
    }
}
