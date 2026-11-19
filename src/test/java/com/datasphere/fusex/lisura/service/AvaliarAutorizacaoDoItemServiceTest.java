package com.datasphere.fusex.lisura.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.contrato.service.CalcularValorContratadoService;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.fatura.service.CalcularDivergenciaValorService;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.lisura.dto.LisuraItemResponse;
import com.datasphere.fusex.ocs.OcsModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvaliarAutorizacaoDoItemServiceTest {

    private static final String CNPJ = "12345678000199";

    private static final LocalDate VALIDADE = LocalDate.of(2026, 10, 10);

    private final AvaliarAutorizacaoDoItemService service = new AvaliarAutorizacaoDoItemService(
            new CalcularDivergenciaValorService(), new CalcularValorContratadoService());

    private EspelhoItemModel item(String valor) {
        EspelhoItemModel i = new EspelhoItemModel();
        i.setId(1);
        i.setDescricao("Sessão");
        i.setValor(new BigDecimal(valor));
        return i;
    }

    private GuiaProcModel atendimento(String valorDoContrato, int sessoes) {
        ContratoModel c = new ContratoModel();
        c.setOcsModel(new OcsModel(CNPJ, "Clínica X"));
        c.setValor(new BigDecimal(valorDoContrato));
        GuiaProcModel a = new GuiaProcModel();
        a.setContratoModel(c);
        a.setQtdAutorizada(sessoes);
        a.setDataValidade(VALIDADE);
        return a;
    }

    @Test
    void valorIgualAoContrato_eAutorizado() {
        LisuraItemResponse r = service.avaliarAutorizacaoDoItem(
                item("150.00"), atendimento("150.00", 1), CNPJ, VALIDADE.minusDays(1));

        assertTrue(r.autorizado());
        assertNull(r.motivo());
    }

    @Test
    void valorAcimaDoContrato_naoEAutorizado() {
        LisuraItemResponse r = service.avaliarAutorizacaoDoItem(
                item("200.00"), atendimento("150.00", 1), CNPJ, VALIDADE.minusDays(1));

        assertFalse(r.autorizado());
        assertEquals("Valor apresentado acima do valor contratado", r.motivo());
    }

    @Test
    void valorAbaixoDoContrato_eAutorizadoPeloValorApresentado() {
        LisuraItemResponse r = service.avaliarAutorizacaoDoItem(
                item("100.00"), atendimento("150.00", 1), CNPJ, VALIDADE.minusDays(1));

        assertTrue(r.autorizado());
        assertNull(r.motivo());
        assertEquals(new BigDecimal("100.00"), r.valorApresentado());
        assertEquals(0, new BigDecimal("150.00").compareTo(r.valorContratado()));
    }

    @Test
    void dezSessoesComparaComDezVezesOContrato() {
        LisuraItemResponse dentro = service.avaliarAutorizacaoDoItem(
                item("800.00"), atendimento("80.00", 10), CNPJ, VALIDADE.minusDays(1));
        LisuraItemResponse acima = service.avaliarAutorizacaoDoItem(
                item("800.01"), atendimento("80.00", 10), CNPJ, VALIDADE.minusDays(1));

        assertTrue(dentro.autorizado());
        assertEquals(0, new BigDecimal("800.00").compareTo(dentro.valorContratado()));
        assertFalse(acima.autorizado());
    }

    @Test
    void atendimentoAposValidade_naoEAutorizado() {
        LisuraItemResponse r = service.avaliarAutorizacaoDoItem(
                item("150.00"), atendimento("150.00", 1), CNPJ, VALIDADE.plusDays(1));

        assertFalse(r.autorizado());
        assertEquals("Guia vencida: atendimento após o prazo de validade", r.motivo());
    }

    @Test
    void atendimentoNoUltimoDiaDeValidade_eAutorizado() {
        LisuraItemResponse r = service.avaliarAutorizacaoDoItem(
                item("150.00"), atendimento("150.00", 1), CNPJ, VALIDADE);

        assertTrue(r.autorizado());
    }

    @Test
    void guiaComValidadeMasSemDataDoAtendimento_naoEAutorizada() {
        LisuraItemResponse r = service.avaliarAutorizacaoDoItem(
                item("150.00"), atendimento("150.00", 1), CNPJ, null);

        assertFalse(r.autorizado());
        assertEquals("Atendimento sem data registrada", r.motivo());
    }

    @Test
    void semContrato_naoEAutorizado() {
        LisuraItemResponse r = service.avaliarAutorizacaoDoItem(item("150.00"), null, CNPJ, null);

        assertFalse(r.autorizado());
    }

    @Test
    void contratoDeOutraOcs_naoEAutorizado() {
        LisuraItemResponse r = service.avaliarAutorizacaoDoItem(
                item("150.00"), atendimento("150.00", 1), "99999999000199", VALIDADE.minusDays(1));

        assertFalse(r.autorizado());
        assertEquals("Procedimento sem contrato com a OCS da fatura", r.motivo());
    }
}
