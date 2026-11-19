package com.datasphere.fusex.lisura.service;

import com.datasphere.fusex.atendimento.SessaoRepository;
import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.divergencia.service.ClassificarSeveridadeDivergenciaService;
import com.datasphere.fusex.divergencia.service.ConsolidarDivergenciasService;
import com.datasphere.fusex.encaminhamento.service.ValidarAuditoriaConcluidaService;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.ItemFaturaModel;
import com.datasphere.fusex.fatura.ItemFaturaRepository;
import com.datasphere.fusex.fatura.service.CalcularDivergenciaValorService;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.ocs.OcsModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GerarLisuraDaFaturaServiceTest {

    @Test
    void sessaoNoPrazoCobradaDepoisComValorMenor_liberaAuditoriaEPreservaDivergencia() {
        String cnpj = "12345678000199";
        LocalDate validade = LocalDate.of(2026, 10, 10);

        EspelhoModel espelho = new EspelhoModel();
        espelho.setId(1);
        espelho.setOcsId(cnpj);
        FaturaModel fatura = new FaturaModel();
        fatura.setId(1L);
        fatura.setEspelhoModel(espelho);

        EspelhoItemModel item = new EspelhoItemModel();
        item.setId(1);
        item.setAtendimentoId(42L);
        item.setValor(new BigDecimal("95.00"));

        ContratoModel contrato = new ContratoModel();
        contrato.setOcsModel(new OcsModel(cnpj, "Clínica X"));
        contrato.setValor(new BigDecimal("100.00"));

        GuiaProcModel atendimento = new GuiaProcModel();
        atendimento.setContratoModel(contrato);
        atendimento.setDataValidade(validade);
        atendimento.setDataValidacao(validade.plusDays(1).atStartOfDay());

        EspelhoItemRepository itens = mock(EspelhoItemRepository.class);
        ConsultarContratoDoItemService consulta = mock(ConsultarContratoDoItemService.class);
        SessaoRepository sessoes = mock(SessaoRepository.class);
        when(itens.findByEspelhoId(1)).thenReturn(List.of(item));
        when(consulta.consultarAtendimentoDoItem(item)).thenReturn(atendimento);
        when(sessoes.findUltimaDataRealizacaoByGuiaProcId(42L))
                .thenReturn(Optional.of(validade.atTime(14, 0)));

        var calcularDivergencia = new CalcularDivergenciaValorService();
        var gerarLisura = new GerarLisuraDaFaturaService(
                itens, consulta, new AvaliarAutorizacaoDoItemService(calcularDivergencia), sessoes,
                mock(ItemFaturaRepository.class));
        var consolidarDivergencias = new ConsolidarDivergenciasService(
                gerarLisura, calcularDivergencia,
                new ClassificarSeveridadeDivergenciaService(new BigDecimal("5"), new BigDecimal("15")));

        var lisura = gerarLisura.gerarLisuraDaFatura(fatura);
        var divergencias = consolidarDivergencias.consolidarDivergencias(fatura);

        assertTrue(new ValidarAuditoriaConcluidaService().validarAuditoriaConcluida(lisura));
        assertEquals(new BigDecimal("95.00"), lisura.itens().getFirst().valorApresentado());
        assertTrue(divergencias.itens().getFirst().autorizado());
        assertEquals(new BigDecimal("-5.00"), divergencias.totalDivergencia());
    }

    @Test
    void faturaMensalIncluiItensDosDoisEspelhos() {
        var cenario = new Cenario("100.00", "100.00");

        var lisura = cenario.service.gerarLisuraDaFatura(cenario.fatura);

        assertEquals(List.of(11, 22), lisura.itens().stream().map(i -> i.espelhoItemId()).toList());
        assertTrue(new ValidarAuditoriaConcluidaService().validarAuditoriaConcluida(lisura));
        verify(cenario.itens, never()).findByEspelhoId(anyInt());
    }

    @Test
    void cobrancaAcimaDoContratoNoSegundoEspelhoTravaAuditoriaMesmoComTotalZero() {
        var cenario = new Cenario("95.00", "105.00");
        var divergencias = new ConsolidarDivergenciasService(cenario.service,
                new CalcularDivergenciaValorService(),
                new ClassificarSeveridadeDivergenciaService(new BigDecimal("5"), new BigDecimal("15")))
                .consolidarDivergencias(cenario.fatura);

        assertEquals(0, divergencias.totalDivergencia().compareTo(BigDecimal.ZERO));
        assertTrue(divergencias.itens().getFirst().autorizado());
        assertFalse(divergencias.itens().getLast().autorizado());
        assertFalse(new ValidarAuditoriaConcluidaService()
                .validarAuditoriaConcluida(cenario.service.gerarLisuraDaFatura(cenario.fatura)));
    }

    @Test
    void cobrancaAcimaDoContratoNoPrimeiroEspelhoTambemTravaAuditoria() {
        var cenario = new Cenario("105.00", "95.00");
        var lisura = cenario.service.gerarLisuraDaFatura(cenario.fatura);

        assertFalse(lisura.itens().getFirst().autorizado());
        assertTrue(lisura.itens().getLast().autorizado());
        assertFalse(new ValidarAuditoriaConcluidaService().validarAuditoriaConcluida(lisura));
    }

    @Test
    void faturaMensalSemReferenciaAoEspelhoLegadoUsaOsVinculos() {
        var cenario = new Cenario("100.00", "100.00");
        cenario.fatura.setEspelhoModel(null);

        assertEquals(2, cenario.service.gerarLisuraDaFatura(cenario.fatura).itens().size());
    }

    @Test
    void ocsDaFaturaTemPrioridadeSobreOEspelhoLegado() {
        var cenario = new Cenario("100.00", "100.00");
        cenario.fatura.getEspelhoModel().setOcsId("98765432000111");

        assertTrue(new ValidarAuditoriaConcluidaService()
                .validarAuditoriaConcluida(cenario.service.gerarLisuraDaFatura(cenario.fatura)));
    }

    @Test
    void vinculoInconsistenteNaoPodeSerIgnoradoNemUsarFallback() {
        var cenario = new Cenario("100.00", "100.00");
        when(cenario.itens.findById(22)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> cenario.service.gerarLisuraDaFatura(cenario.fatura));
        verify(cenario.itens, never()).findByEspelhoId(anyInt());
    }

    private static class Cenario {
        private final EspelhoItemRepository itens = mock(EspelhoItemRepository.class);
        private final ItemFaturaRepository vinculos = mock(ItemFaturaRepository.class);
        private final ConsultarContratoDoItemService consulta = mock(ConsultarContratoDoItemService.class);
        private final SessaoRepository sessoes = mock(SessaoRepository.class);
        private final FaturaModel fatura = new FaturaModel();
        private final GerarLisuraDaFaturaService service = new GerarLisuraDaFaturaService(
                itens, consulta, new AvaliarAutorizacaoDoItemService(new CalcularDivergenciaValorService()),
                sessoes, vinculos);

        private Cenario(String primeiroValor, String segundoValor) {
            String cnpj = "12345678000199";
            fatura.setId(1L);
            fatura.setOcsId(cnpj);
            EspelhoModel espelho = new EspelhoModel();
            espelho.setId(1);
            espelho.setOcsId(cnpj);
            fatura.setEspelhoModel(espelho);
            var primeiro = prepararItem(11, 1, primeiroValor, cnpj);
            var segundo = prepararItem(22, 2, segundoValor, cnpj);
            when(vinculos.findByFaturaId(1L)).thenReturn(List.of(primeiro, segundo));
        }

        private ItemFaturaModel prepararItem(int id, int espelhoId, String valor, String cnpj) {
            EspelhoItemModel item = new EspelhoItemModel();
            item.setId(id);
            item.setEspelhoId(espelhoId);
            item.setAtendimentoId((long) id);
            item.setValor(new BigDecimal(valor));
            when(itens.findById(id)).thenReturn(Optional.of(item));
            ContratoModel contrato = new ContratoModel();
            contrato.setOcsModel(new OcsModel(cnpj, "Clínica X"));
            contrato.setValor(new BigDecimal("100.00"));
            GuiaProcModel atendimento = new GuiaProcModel();
            atendimento.setContratoModel(contrato);
            atendimento.setDataValidade(LocalDate.of(2026, 10, 10));
            atendimento.setDataValidacao(LocalDate.of(2026, 10, 9).atTime(14, 0));
            when(consulta.consultarAtendimentoDoItem(item)).thenReturn(atendimento);
            ItemFaturaModel vinculo = new ItemFaturaModel();
            vinculo.setFaturaId(1L);
            vinculo.setEspelhoItemId(id);
            return vinculo;
        }
    }
}
