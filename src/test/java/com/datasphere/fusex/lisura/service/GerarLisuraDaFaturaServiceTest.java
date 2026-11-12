package com.datasphere.fusex.lisura.service;

import com.datasphere.fusex.atendimento.sessao.SessaoRepository;
import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.divergencia.service.ClassificarSeveridadeDivergenciaService;
import com.datasphere.fusex.divergencia.service.ConsolidarDivergenciasService;
import com.datasphere.fusex.encaminhamento.service.ValidarAuditoriaConcluidaService;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemRepository;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.service.CalcularDivergenciaValorService;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.ocs.OcsModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
                itens, consulta, new AvaliarAutorizacaoDoItemService(calcularDivergencia), sessoes);
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
}
