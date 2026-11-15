package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.exception.QrCodeIndisponivelException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.service.ValidadeGuiaService;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarQrDoProcedimentoServiceTest {

    @Mock private GuiaProcRepository repository;
    @Mock private ValidarDonoDaGuiaService validarDono;
    private final Principal principal = () -> "login";
    private BuscarQrDoProcedimentoService service;
    private GuiaModel guia;
    private GuiaProcModel procedimento;

    @BeforeEach
    void configurar() {
        service = new BuscarQrDoProcedimentoService(repository, validarDono, new ValidadeGuiaService(60));
        guia = new GuiaModel();
        guia.setBenfId("dono");
        guia.setStatus("EMITIDA");
        guia.setDataEmissao(LocalDate.now());
        procedimento = new GuiaProcModel();
        procedimento.setId(10L);
        procedimento.setGuiaModel(guia);
        procedimento.setStatus("EM_ANDAMENTO");
        procedimento.setQtdAutorizada(2);
        procedimento.setQtdRealizada(2);
        procedimento.setQr("codigo-salvo");
        when(validarDono.identificarBeneficiario(principal)).thenReturn("dono");
    }

    private void encontrado() {
        when(repository.findById(10L)).thenReturn(Optional.of(procedimento));
    }

    private void verificarConflito(ErrorCode codigo) {
        var erro = assertThrows(QrCodeIndisponivelException.class,
                () -> service.buscarQrDoProcedimento(10L, principal));
        assertEquals(codigo, erro.getErrorCode());
        verify(repository, never()).save(any());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void devolveCodigoPersistidoSemGravarOuGerarOutro() {
        encontrado();
        assertEquals("codigo-salvo", service.buscarQrDoProcedimento(10L, principal).codigo());
        verify(validarDono).validarDonoDaGuia(guia, "dono");
        verify(repository).findById(10L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void outroBeneficiarioEVerificadoAntesDeExporEstadoDoProcedimento() {
        encontrado();
        procedimento.setStatus("REALIZADA");
        doThrow(new BusinessException(ErrorCode.BENEFICIARIO_SEM_PERMISSAO))
                .when(validarDono).validarDonoDaGuia(guia, "dono");
        var erro = assertThrows(BusinessException.class, () -> service.buscarQrDoProcedimento(10L, principal));
        assertEquals(ErrorCode.BENEFICIARIO_SEM_PERMISSAO, erro.getErrorCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"REALIZADA", "REALIZADO", "FATURADA"})
    void realizadoOuFaturadoRetornaConflito(String status) {
        encontrado();
        procedimento.setStatus(status);
        verificarConflito(ErrorCode.PROCEDIMENTO_JA_REALIZADO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"EXPIRADA", "EXPIRADO"})
    void procedimentoExpiradoRetornaConflito(String status) {
        encontrado();
        procedimento.setStatus(status);
        verificarConflito(ErrorCode.GUIA_EXPIRADA);
    }

    @Test
    void guiaVencidaPelaDataRetornaConflito() {
        encontrado();
        guia.setDataEmissao(LocalDate.now().minusDays(61));
        verificarConflito(ErrorCode.GUIA_EXPIRADA);
    }

    @Test
    void guiaComStatusExpiradaRetornaConflito() {
        encontrado();
        guia.setStatus("EXPIRADA");
        verificarConflito(ErrorCode.GUIA_EXPIRADA);
    }

    @Test
    void sessoesIncompletasRetornamConflito() {
        encontrado();
        procedimento.setQtdRealizada(1);
        verificarConflito(ErrorCode.SESSOES_INCOMPLETAS);
    }

    @Test
    void sessaoUnicaTambemPrecisaEstarConcluida() {
        encontrado();
        procedimento.setQtdAutorizada(1);
        procedimento.setQtdRealizada(0);
        verificarConflito(ErrorCode.SESSOES_INCOMPLETAS);
    }

    @Test
    void quantidadeAusenteNaoLiberaQr() {
        encontrado();
        procedimento.setQtdRealizada(null);
        verificarConflito(ErrorCode.SESSOES_INCOMPLETAS);
    }

    @Test
    void qrAusenteNaoEGeradoNaConsulta() {
        encontrado();
        procedimento.setQr(null);
        verificarConflito(ErrorCode.QR_NAO_GERADO);
    }

    @Test
    void guiaNoSexagesimoDiaAindaPodeSerConsultada() {
        encontrado();
        guia.setDataEmissao(LocalDate.now().minusDays(60));
        assertEquals("codigo-salvo", service.buscarQrDoProcedimento(10L, principal).codigo());
    }

    @Test
    void atendimentoAusenteMantem404() {
        when(repository.findById(10L)).thenReturn(Optional.empty());
        var erro = assertThrows(BusinessException.class, () -> service.buscarQrDoProcedimento(10L, principal));
        assertEquals(ErrorCode.ATENDIMENTO_NAO_ENCONTRADO, erro.getErrorCode());
    }
}
