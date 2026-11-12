package com.datasphere.fusex.lisura.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.fatura.service.CalcularDivergenciaValorService;
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

    private final AvaliarAutorizacaoDoItemService service =
            new AvaliarAutorizacaoDoItemService(new CalcularDivergenciaValorService());

    private EspelhoItemModel item(String valor) {
        EspelhoItemModel i = new EspelhoItemModel();
        i.setId(1);
        i.setDescricao("Sessão");
        i.setValor(new BigDecimal(valor));
        return i;
    }

    private ContratoModel contrato(String valor) {
        ContratoModel c = new ContratoModel();
        c.setOcsModel(new OcsModel(CNPJ, "Clínica X"));
        c.setValor(new BigDecimal(valor));
        return c;
    }

    @Test
    void valorIgualAoContrato_eAutorizado() {
        LisuraItemResponse r = service.avaliarAutorizacaoDoItem(
                item("150.00"), contrato("150.00"), CNPJ, VALIDADE, VALIDADE.minusDays(1));

        assertTrue(r.autorizado());
        assertNull(r.motivo());
    }

    @Test
    void valorAcimaDoContrato_naoEAutorizado() {
        LisuraItemResponse r = service.avaliarAutorizacaoDoItem(
                item("200.00"), contrato("150.00"), CNPJ, VALIDADE, VALIDADE.minusDays(1));

        assertFalse(r.autorizado());
        assertEquals("Valor apresentado acima do valor contratado", r.motivo());
    }

    @Test
    void valorAbaixoDoContrato_eAutorizadoPeloValorApresentado() {
        LisuraItemResponse r = service.avaliarAutorizacaoDoItem(
                item("100.00"), contrato("150.00"), CNPJ, VALIDADE, VALIDADE.minusDays(1));

        assertTrue(r.autorizado());
        assertNull(r.motivo());
        assertEquals(new BigDecimal("100.00"), r.valorApresentado());
        assertEquals(new BigDecimal("150.00"), r.valorContratado());
    }

    @Test
    void atendimentoAposValidade_naoEAutorizado() {
        LisuraItemResponse r = service.avaliarAutorizacaoDoItem(
                item("150.00"), contrato("150.00"), CNPJ, VALIDADE, VALIDADE.plusDays(1));

        assertFalse(r.autorizado());
        assertEquals("Guia vencida: atendimento após o prazo de validade", r.motivo());
    }

    @Test
    void atendimentoNoUltimoDiaDeValidade_eAutorizado() {
        LisuraItemResponse r = service.avaliarAutorizacaoDoItem(
                item("150.00"), contrato("150.00"), CNPJ, VALIDADE, VALIDADE);

        assertTrue(r.autorizado());
    }

    @Test
    void guiaComValidadeMasSemDataDoAtendimento_naoEAutorizada() {
        LisuraItemResponse r = service.avaliarAutorizacaoDoItem(
                item("150.00"), contrato("150.00"), CNPJ, VALIDADE, null);

        assertFalse(r.autorizado());
        assertEquals("Atendimento sem data registrada", r.motivo());
    }

    @Test
    void semContrato_naoEAutorizado() {
        LisuraItemResponse r = service.avaliarAutorizacaoDoItem(item("150.00"), null, CNPJ, null, null);

        assertFalse(r.autorizado());
    }
}