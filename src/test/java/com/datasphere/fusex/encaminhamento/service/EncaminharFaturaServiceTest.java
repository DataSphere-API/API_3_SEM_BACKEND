package com.datasphere.fusex.encaminhamento.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.fatura.dto.FaturaMensalDetalheResponse.Pendente;
import com.datasphere.fusex.fatura.service.ConsultarFaturaMensalService;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EncaminharFaturaServiceTest {

    private final EntityManager entityManager = mock(EntityManager.class);
    private final ConsultarFaturaMensalService consulta = mock(ConsultarFaturaMensalService.class);
    private final RegistrarMudancaStatusService historico = mock(RegistrarMudancaStatusService.class);
    private final EncaminharFaturaService service =
            new EncaminharFaturaService(entityManager, consulta, new ValidarMesEncerradoService(), historico);

    @Test
    void mesAtualDevolve422() {
        prepararFatura(YearMonth.now());
        var erro = assertThrows(BusinessException.class, () -> service.encaminharFatura(1L));
        assertEquals(ErrorCode.MES_NAO_ENCERRADO, erro.getErrorCode());
        assertEquals(422, erro.getErrorCode().getStatus().value());
        verifyNoInteractions(historico);
    }

    @Test
    void espelhoPendenteDevolve422() {
        var fatura = prepararFatura(YearMonth.now().minusMonths(2));
        when(consulta.listarPendentes(fatura)).thenReturn(List.of(new Pendente(5, "REPROVADO")));
        var erro = assertThrows(BusinessException.class, () -> service.encaminharFatura(1L));
        assertEquals(ErrorCode.AUDITORIA_PENDENTE, erro.getErrorCode());
        assertEquals(422, erro.getErrorCode().getStatus().value());
        verifyNoInteractions(historico);
    }

    @Test
    void mesEncerradoSemPendenciasAtualizaStatusEHistorico() {
        var fatura = prepararFatura(YearMonth.now().minusMonths(2));
        when(consulta.listarPendentes(fatura)).thenReturn(List.of());

        service.encaminharFatura(1L);

        assertEquals(StatusFatura.ENCAMINHADA_APROVACAO, fatura.getStatus());
        verify(historico).executar(EntidadeTipo.FATURA, 1L, "ATIVA",
                "ENCAMINHADA_APROVACAO", "encaminhamento-para-aprovacao");
    }

    private FaturaModel prepararFatura(YearMonth competencia) {
        var fatura = new FaturaModel();
        fatura.setStatus(StatusFatura.ATIVA);
        when(entityManager.find(FaturaModel.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(fatura);
        when(consulta.competenciaDaFatura(fatura)).thenReturn(competencia);
        return fatura;
    }
}
