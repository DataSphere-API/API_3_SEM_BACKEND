package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.FaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TransicionarStatusDaFaturaServiceTest {

    private final FaturaRepository faturas = mock(FaturaRepository.class);
    private final RegistrarMudancaStatusService historico = mock(RegistrarMudancaStatusService.class);
    private final TransicionarStatusDaFaturaService service = new TransicionarStatusDaFaturaService(faturas, historico);

    @Test
    void aprovaELiquidaEmOrdemComHistorico() {
        var fatura = fatura(StatusFatura.ENCAMINHADA_APROVACAO);

        service.aprovarFatura(1L);
        assertEquals(StatusFatura.APROVADA, fatura.getStatus());
        service.liquidarFatura(1L);
        assertEquals(StatusFatura.LIQUIDADA, fatura.getStatus());

        verify(historico).executar(EntidadeTipo.FATURA, 1L, "ENCAMINHADA_APROVACAO", "APROVADA", "aprovacao-da-fatura");
        verify(historico).executar(EntidadeTipo.FATURA, 1L, "APROVADA", "LIQUIDADA", "liquidacao-da-fatura");
    }

    @Test
    void rejeitaLiquidarAntesDeAprovar() {
        var fatura = fatura(StatusFatura.ENCAMINHADA_APROVACAO);
        var erro = assertThrows(BusinessException.class, () -> service.liquidarFatura(1L));

        assertEquals(409, erro.getErrorCode().getStatus().value());
        assertEquals(StatusFatura.ENCAMINHADA_APROVACAO, fatura.getStatus());
        verifyNoInteractions(historico);
    }

    @Test
    void rejeitaVoltarDeLiquidadaParaAprovada() {
        fatura(StatusFatura.LIQUIDADA);
        var erro = assertThrows(BusinessException.class, () -> service.aprovarFatura(1L));

        assertEquals(ErrorCode.FATURA_TRANSICAO_INVALIDA, erro.getErrorCode());
        verifyNoInteractions(historico);
    }

    private FaturaModel fatura(StatusFatura status) {
        var fatura = new FaturaModel();
        fatura.setStatus(status);
        when(faturas.findByIdParaAlterarStatus(1L)).thenReturn(Optional.of(fatura));
        return fatura;
    }
}
