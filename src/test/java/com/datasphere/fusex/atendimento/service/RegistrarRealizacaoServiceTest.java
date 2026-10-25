package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.dto.request.BaixaResponse;
import com.datasphere.fusex.dto.request.ValidarQrCodeRequest;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.service.ValidacaoQrService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarRealizacaoServiceTest {

    private static final String QR = "QR1";
    private static final String CNPJ = "12345678000101";
    private static final ValidarQrCodeRequest REQUEST = new ValidarQrCodeRequest(QR, CNPJ);

    @Mock
    private GuiaProcRepository guiaProcRepository;
    @Mock
    private ValidacaoQrService validacaoQrService;
    @Mock
    private AtualizarStatusGuiaProcRealizadaService atualizarStatus;
    @Mock
    private RecalcularStatusDaGuiaService recalcularStatusDaGuia;

    @InjectMocks
    private RegistrarRealizacaoService service;

    private GuiaProcModel procedimento(int qtdAutorizada, int qtdRealizada) {
        GuiaModel guia = new GuiaModel();
        guia.setId(4L);

        GuiaProcModel p = new GuiaProcModel();
        p.setGuiaId(10L);
        p.setGuiaModel(guia);
        p.setQr(QR);
        p.setStatus("Pendente");
        p.setQtdAutorizada(qtdAutorizada);
        p.setQtdRealizada(qtdRealizada);
        return p;
    }

    private void simularAtualizacao() {
        when(atualizarStatus.atualizarStatusGuiaProcRealizada(any())).thenAnswer(inv -> {
            GuiaProcModel p = inv.getArgument(0);
            p.setStatus("Realizada");
            p.setDataValidacao(LocalDateTime.of(2026, 11, 20, 10, 5));
            return p;
        });
    }

    @Test
    void qrInexistente_lancaErroENaoAlteraNada() {
        when(guiaProcRepository.findByQrParaBaixa(QR)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.registrarRealizacao(REQUEST));

        assertEquals(ErrorCode.QR_NAO_ENCONTRADO, ex.getErrorCode());
        verifyNoInteractions(validacaoQrService, atualizarStatus, recalcularStatusDaGuia);
    }

    @Test
    void validacaoFalha_naoAlteraNada() {
        GuiaProcModel p = procedimento(1, 0);
        when(guiaProcRepository.findByQrParaBaixa(QR)).thenReturn(Optional.of(p));
        doThrow(new BusinessException(ErrorCode.GUIA_EXPIRADA))
                .when(validacaoQrService).validarGuiaDentroDaValidade(p);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.registrarRealizacao(REQUEST));

        assertEquals(ErrorCode.GUIA_EXPIRADA, ex.getErrorCode());
        assertEquals(0, p.getQtdRealizada());
        verifyNoInteractions(atualizarStatus, recalcularStatusDaGuia);
    }

    @Test
    void sessoesIncompletas_lancaErroComDetalhesENaoConcluiBaixa() {
        GuiaProcModel p = procedimento(10, 3);
        when(guiaProcRepository.findByQrParaBaixa(QR)).thenReturn(Optional.of(p));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.registrarRealizacao(REQUEST));

        assertEquals(ErrorCode.SESSOES_INCOMPLETAS, ex.getErrorCode());
        assertEquals(Map.of("qtdRealizada", 3, "qtdAutorizada", 10, "saldo", 7), ex.getDetalhes());
        assertEquals(3, p.getQtdRealizada());
        verifyNoInteractions(atualizarStatus, recalcularStatusDaGuia);
    }

    @Test
    void sessaoUnica_registraSessaoEConcluiNoMesmoFluxo() {
        GuiaProcModel p = procedimento(1, 0);
        when(guiaProcRepository.findByQrParaBaixa(QR)).thenReturn(Optional.of(p));
        simularAtualizacao();
        when(recalcularStatusDaGuia.recalcularStatusDaGuia(4L)).thenReturn("FINALIZADA");

        BaixaResponse r = service.registrarRealizacao(REQUEST);

        assertEquals(1, p.getQtdRealizada());
        verify(atualizarStatus).atualizarStatusGuiaProcRealizada(p);
        verify(recalcularStatusDaGuia).recalcularStatusDaGuia(4L);
        assertEquals(10L, r.atendimentoId());
        assertEquals("Realizada", r.statusProcedimento());
        assertEquals(1, r.qtdAutorizada());
        assertEquals(1, r.qtdRealizada());
        assertNotNull(r.dataValidacao());
        assertEquals("FINALIZADA", r.statusGuia());
    }

    @Test
    void todasSessoesCompletas_concluiBaixaEGuiaPodeFicarParcial() {
        GuiaProcModel p = procedimento(10, 10);
        when(guiaProcRepository.findByQrParaBaixa(QR)).thenReturn(Optional.of(p));
        simularAtualizacao();
        when(recalcularStatusDaGuia.recalcularStatusDaGuia(4L)).thenReturn("EMITIDA");

        BaixaResponse r = service.registrarRealizacao(REQUEST);

        assertEquals(10, r.qtdAutorizada());
        assertEquals(10, r.qtdRealizada());
        assertEquals("Realizada", r.statusProcedimento());
        assertEquals("EMITIDA", r.statusGuia());
    }
}