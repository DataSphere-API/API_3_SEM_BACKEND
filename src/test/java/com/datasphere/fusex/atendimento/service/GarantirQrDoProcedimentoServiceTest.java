package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GarantirQrDoProcedimentoServiceTest {

    @Mock private GuiaProcRepository guiaProcRepository;
    @Mock private GerarCodigoUnicoQrService gerarCodigoUnicoQrService;
    @Mock private SalvarQrNoProcedimentoService salvarQrNoProcedimentoService;

    private GarantirQrDoProcedimentoService service;
    private GuiaModel guia;
    private GuiaProcModel procedimento;

    @BeforeEach
    void configurar() {
        service = new GarantirQrDoProcedimentoService(guiaProcRepository, gerarCodigoUnicoQrService,
                salvarQrNoProcedimentoService, new ValidarProcedimentoNaoRealizadoService(),
                new ValidadeGuiaService(60));
        guia = new GuiaModel();
        guia.setId(1L);
        guia.setStatus("EMITIDA");
        guia.setDataEmissao(LocalDate.now().minusDays(10));
        procedimento = new GuiaProcModel();
        procedimento.setId(10L);
        procedimento.setGuiaModel(guia);
        procedimento.setStatus("PENDENTE");
        procedimento.setDataValidade(LocalDate.now().plusDays(50));
    }

    private void encontrado() {
        when(guiaProcRepository.findByIdParaQr(10L)).thenReturn(Optional.of(procedimento));
    }

    private void verificarErro(ErrorCode codigo) {
        BusinessException erro = assertThrows(BusinessException.class, () -> service.garantirQrDoProcedimento(10L));
        assertEquals(codigo, erro.getErrorCode());
        verifyNoInteractions(gerarCodigoUnicoQrService, salvarQrNoProcedimentoService);
    }

    @Test
    void semQrGeraESalvaOCodigo() {
        encontrado();
        when(gerarCodigoUnicoQrService.gerarCodigoUnicoQr()).thenReturn("uuid-novo");
        assertEquals("uuid-novo", service.garantirQrDoProcedimento(10L));
        verify(salvarQrNoProcedimentoService).salvarQrNoProcedimento(procedimento, "uuid-novo");
    }

    @Test
    void qrExistenteEReaproveitadoSemGerarOuGravarNovamente() {
        encontrado();
        procedimento.setQr("codigo-existente");
        assertEquals("codigo-existente", service.garantirQrDoProcedimento(10L));
        verifyNoInteractions(gerarCodigoUnicoQrService, salvarQrNoProcedimentoService);
    }

    @Test
    void qrEmBrancoEGeradoNovamente() {
        encontrado();
        procedimento.setQr("  ");
        when(gerarCodigoUnicoQrService.gerarCodigoUnicoQr()).thenReturn("uuid-novo");
        assertEquals("uuid-novo", service.garantirQrDoProcedimento(10L));
        verify(salvarQrNoProcedimentoService).salvarQrNoProcedimento(procedimento, "uuid-novo");
    }

    @Test
    void atendimentoAusenteRetornaErroDeDominio() {
        when(guiaProcRepository.findByIdParaQr(10L)).thenReturn(Optional.empty());
        verificarErro(ErrorCode.ATENDIMENTO_NAO_ENCONTRADO);
    }

    @Test
    void idNuloRetornaAtendimentoNaoEncontrado() {
        BusinessException erro = assertThrows(BusinessException.class, () -> service.garantirQrDoProcedimento(null));
        assertEquals(ErrorCode.ATENDIMENTO_NAO_ENCONTRADO, erro.getErrorCode());
        verifyNoInteractions(guiaProcRepository, gerarCodigoUnicoQrService, salvarQrNoProcedimentoService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"REALIZADA", "FATURADA", "REALIZADO"})
    void procedimentoConcluidoNaoPodeGerarNemReaproveitarQr(String status) {
        encontrado();
        procedimento.setStatus(status);
        procedimento.setQr("codigo-existente");
        verificarErro(ErrorCode.PROCEDIMENTO_JA_REALIZADO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"EXPIRADA", "EXPIRADO"})
    void procedimentoExpiradoRetornaGuiaExpirada(String status) {
        encontrado();
        procedimento.setStatus(status);
        verificarErro(ErrorCode.GUIA_EXPIRADA);
    }

    @ParameterizedTest
    @ValueSource(strings = {"EMITIDA", "PARCIALMENTE_REALIZADA"})
    void aceitaOsDoisStatusDeGuiaDoContrato(String status) {
        encontrado();
        guia.setStatus(status);
        procedimento.setStatus("EM_ANDAMENTO");
        procedimento.setQr("codigo-existente");
        assertEquals("codigo-existente", service.garantirQrDoProcedimento(10L));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 59, 60})
    void permiteAteOSexagesimoDiaInclusive(int dias) {
        encontrado();
        guia.setDataEmissao(LocalDate.now().minusDays(dias));
        procedimento.setDataValidade(null);
        procedimento.setQr("codigo-existente");
        assertEquals("codigo-existente", service.garantirQrDoProcedimento(10L));
    }

    @Test
    void calculaExpiracaoPelaEmissaoMesmoSemValidadeNoProcedimento() {
        encontrado();
        guia.setDataEmissao(LocalDate.now().minusDays(61));
        procedimento.setDataValidade(null);
        procedimento.setQr("codigo-existente");
        verificarErro(ErrorCode.GUIA_EXPIRADA);
    }

    @Test
    void validadeDoProcedimentoNaoPodeEstenderOsSessentaDiasDaGuia() {
        encontrado();
        guia.setDataEmissao(LocalDate.now().minusDays(61));
        procedimento.setDataValidade(LocalDate.now().plusDays(100));
        verificarErro(ErrorCode.GUIA_EXPIRADA);
    }

    @Test
    void respeitaValidadeMenorDoProcedimento() {
        encontrado();
        procedimento.setDataValidade(LocalDate.now().minusDays(1));
        verificarErro(ErrorCode.GUIA_EXPIRADA);
    }

    @Test
    void guiaExpiradaBloqueiaMesmoComDataRecente() {
        encontrado();
        guia.setStatus("EXPIRADA");
        verificarErro(ErrorCode.GUIA_EXPIRADA);
    }

    @ParameterizedTest
    @ValueSource(strings = {"FINALIZADA", "PRE_GUIA", ""})
    void guiaForaDosStatusPermitidosNaoGeraQr(String status) {
        encontrado();
        guia.setStatus(status);
        verificarErro(ErrorCode.GUIA_NAO_EMITIDA);
    }

    @Test
    void semGuiaNaoGeraQr() {
        encontrado();
        procedimento.setGuiaModel(null);
        verificarErro(ErrorCode.GUIA_NAO_EMITIDA);
    }

    @Test
    void semDataDeEmissaoNaoConsideraAValidadeIlimitada() {
        encontrado();
        guia.setDataEmissao(null);
        verificarErro(ErrorCode.GUIA_EXPIRADA);
    }

    @Test
    void dataDeEmissaoFuturaNaoPermiteGerarQr() {
        encontrado();
        guia.setDataEmissao(LocalDate.now().plusDays(1));
        verificarErro(ErrorCode.GUIA_EXPIRADA);
    }
}
