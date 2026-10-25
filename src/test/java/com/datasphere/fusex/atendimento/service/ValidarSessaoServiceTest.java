package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.ocs.OcsModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidarSessaoServiceTest {

    private ValidarSessaoService validarSessaoService;

    @BeforeEach
    void setUp() {
        validarSessaoService = new ValidarSessaoService();
    }

    // ============================================================
    // validarOcsDoContrato
    // ============================================================
    @Test
    @DisplayName("validarOcsDoContrato: deve passar quando CNPJ bate com o do contrato")
    void devePassarQuandoCnpjBate() {
        GuiaProcModel atendimento = atendimentoComCnpj("12345678000199");

        assertThatCode(() -> validarSessaoService.validarOcsDoContrato(atendimento, "12345678000199"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarOcsDoContrato: deve lançar erro quando CNPJ é diferente")
    void deveLancarErroQuandoCnpjDiferente() {
        GuiaProcModel atendimento = atendimentoComCnpj("12345678000199");

        assertThatThrownBy(() -> validarSessaoService.validarOcsDoContrato(atendimento, "99999999999999"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OCS_DIFERENTE_DO_CONTRATO);
    }

    @Test
    @DisplayName("validarOcsDoContrato: deve lançar erro quando CNPJ é nulo/vazio")
    void deveLancarErroQuandoCnpjNulo() {
        GuiaProcModel atendimento = atendimentoComCnpj("12345678000199");

        assertThatThrownBy(() -> validarSessaoService.validarOcsDoContrato(atendimento, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OCS_DIFERENTE_DO_CONTRATO);
    }

    // ============================================================
    // validarGuiaEmitida
    // ============================================================
    @Test
    @DisplayName("validarGuiaEmitida: deve passar com guia EMITIDA")
    void devePassarComGuiaEmitida() {
        GuiaProcModel atendimento = atendimentoComStatusGuia("EMITIDA");

        assertThatCode(() -> validarSessaoService.validarGuiaEmitida(atendimento))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarGuiaEmitida: deve rejeitar com guia PARCIALMENTE_REALIZADA")
    void rejeitaComGuiaParcialmenteRealizada() {
        GuiaProcModel atendimento = atendimentoComStatusGuia("PARCIALMENTE_REALIZADA");

        assertThatThrownBy(() -> validarSessaoService.validarGuiaEmitida(atendimento))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GUIA_NAO_EMITIDA);
    }

    @Test
    @DisplayName("validarGuiaEmitida: deve lançar GUIA_NAO_EMITIDA quando status é nulo")
    void deveLancarErroQuandoStatusGuiaNulo() {
        GuiaProcModel atendimento = atendimentoComStatusGuia(null);

        assertThatThrownBy(() -> validarSessaoService.validarGuiaEmitida(atendimento))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GUIA_NAO_EMITIDA);
    }

    @Test
    @DisplayName("validarGuiaEmitida: deve lançar GUIA_NAO_EMITIDA quando status é CANCELADA")
    void deveLancarErroQuandoGuiaCancelada() {
        GuiaProcModel atendimento = atendimentoComStatusGuia("CANCELADA");

        assertThatThrownBy(() -> validarSessaoService.validarGuiaEmitida(atendimento))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GUIA_NAO_EMITIDA);
    }

    // ============================================================
    // validarGuiaDentroDaValidade
    // ============================================================
    @Test
    @DisplayName("validarGuiaDentroDaValidade: deve passar quando validade é futura")
    void devePassarQuandoValidadeFutura() {
        GuiaProcModel atendimento = new GuiaProcModel();
        atendimento.setDataValidade(LocalDate.now().plusDays(10));

        assertThatCode(() -> validarSessaoService.validarGuiaDentroDaValidade(atendimento))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarGuiaDentroDaValidade: deve passar quando validade é nula")
    void devePassarQuandoValidadeNula() {
        GuiaProcModel atendimento = new GuiaProcModel();
        atendimento.setDataValidade(null);

        assertThatCode(() -> validarSessaoService.validarGuiaDentroDaValidade(atendimento))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarGuiaDentroDaValidade: deve lançar GUIA_EXPIRADA quando validade passou")
    void deveLancarErroQuandoValidadePassou() {
        GuiaProcModel atendimento = new GuiaProcModel();
        atendimento.setDataValidade(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> validarSessaoService.validarGuiaDentroDaValidade(atendimento))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GUIA_EXPIRADA);
    }

    // ============================================================
    // validarSaldoDeSessoes
    // ============================================================
    @Test
    @DisplayName("validarSaldoDeSessoes: deve passar quando há saldo")
    void devePassarQuandoHaSaldo() {
        GuiaProcModel atendimento = new GuiaProcModel();
        atendimento.setQtdAutorizada(10);
        atendimento.setQtdRealizada(3);

        assertThatCode(() -> validarSessaoService.validarSaldoDeSessoes(atendimento))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarSaldoDeSessoes: deve lançar SESSOES_ESGOTADAS quando realizado == autorizado")
    void deveLancarErroQuandoRealizadoIgualAutorizado() {
        GuiaProcModel atendimento = new GuiaProcModel();
        atendimento.setQtdAutorizada(10);
        atendimento.setQtdRealizada(10);

        assertThatThrownBy(() -> validarSessaoService.validarSaldoDeSessoes(atendimento))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SESSOES_ESGOTADAS);
    }

    @Test
    @DisplayName("validarSaldoDeSessoes: deve lançar SESSOES_ESGOTADAS quando realizado > autorizado")
    void deveLancarErroQuandoRealizadoMaiorAutorizado() {
        GuiaProcModel atendimento = new GuiaProcModel();
        atendimento.setQtdAutorizada(10);
        atendimento.setQtdRealizada(11);

        assertThatThrownBy(() -> validarSessaoService.validarSaldoDeSessoes(atendimento))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SESSOES_ESGOTADAS);
    }

    // ============================================================
    // Helpers
    // ============================================================
    private GuiaProcModel atendimentoComCnpj(String cnpj) {
        OcsModel ocs = new OcsModel();
        ocs.setCnpj(cnpj);
        ocs.setNome("OCS Teste");

        ContratoModel contrato = new ContratoModel();
        contrato.setOcsModel(ocs);

        GuiaProcModel atendimento = new GuiaProcModel();
        atendimento.setContratoModel(contrato);
        return atendimento;
    }

    private GuiaProcModel atendimentoComStatusGuia(String status) {
        GuiaModel guia = new GuiaModel();
        guia.setStatus(status);

        GuiaProcModel atendimento = new GuiaProcModel();
        atendimento.setGuiaModel(guia);
        return atendimento;
    }
}