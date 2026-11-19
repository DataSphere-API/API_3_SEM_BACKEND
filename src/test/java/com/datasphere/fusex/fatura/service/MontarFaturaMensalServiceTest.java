package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.encaminhamento.service.ValidarAuditoriaConcluidaService;
import com.datasphere.fusex.encaminhamento.service.ValidarMesEncerradoService;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.ItemFaturaModel;
import com.datasphere.fusex.fatura.ItemFaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.fatura.dto.FaturaMensalDetalheResponse;
import com.datasphere.fusex.lisura.service.GerarLisuraDaFaturaService;
import com.datasphere.fusex.ocs.OcsModel;
import com.datasphere.fusex.ocs.OcsRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MontarFaturaMensalServiceTest {

    @Test
    void agrupaOsItensPorEspelhoSomaOsValoresENaoDeixaEncaminharComPendente() {
        FaturaModel fatura = new FaturaModel();
        fatura.setId(1L);
        fatura.setOcsId("12345678000199");
        fatura.setCompetencia("2026-10");
        fatura.setStatus(StatusFatura.ATIVA);

        ItemFaturaModel primeiro = new ItemFaturaModel(10, 1L, 100,
                new BigDecimal("110.00"), new BigDecimal("100.00"), new BigDecimal("10.00"));
        ItemFaturaModel segundo = new ItemFaturaModel(11, 1L, 200,
                new BigDecimal("50.00"), new BigDecimal("50.00"), new BigDecimal("0.00"));

        EspelhoItemModel itemEspelho1 = new EspelhoItemModel();
        itemEspelho1.setId(100);
        itemEspelho1.setEspelhoId(7);
        itemEspelho1.setAtendimentoId(42L);
        EspelhoItemModel itemEspelho2 = new EspelhoItemModel();
        itemEspelho2.setId(200);
        itemEspelho2.setEspelhoId(8);
        itemEspelho2.setAtendimentoId(43L);

        EspelhoModel espelho7 = new EspelhoModel();
        espelho7.setId(7);
        espelho7.setDataInicio(LocalDate.of(2026, 10, 1));
        EspelhoModel espelho8 = new EspelhoModel();
        espelho8.setId(8);
        espelho8.setDataInicio(LocalDate.of(2026, 10, 15));
        EspelhoModel pendente = new EspelhoModel();
        pendente.setId(9);
        pendente.setStatus(StatusEspelho.EM_ABERTO);

        ItemFaturaRepository itensFatura = mock(ItemFaturaRepository.class);
        EspelhoItemRepository itensEspelho = mock(EspelhoItemRepository.class);
        EspelhoRepository espelhos = mock(EspelhoRepository.class);
        OcsRepository ocs = mock(OcsRepository.class);
        ListarEspelhosPendentesService pendentes = mock(ListarEspelhosPendentesService.class);
        when(itensFatura.findByFaturaId(1L)).thenReturn(List.of(primeiro, segundo));
        when(itensEspelho.findById(100)).thenReturn(Optional.of(itemEspelho1));
        when(itensEspelho.findById(200)).thenReturn(Optional.of(itemEspelho2));
        when(espelhos.findById(7)).thenReturn(Optional.of(espelho7));
        when(espelhos.findById(8)).thenReturn(Optional.of(espelho8));
        when(ocs.findById("12345678000199")).thenReturn(Optional.of(new OcsModel("12345678000199", "Clínica X")));
        when(pendentes.listar(fatura)).thenReturn(List.of(pendente));

        MontarFaturaMensalService service = new MontarFaturaMensalService(itensFatura, itensEspelho, espelhos, ocs,
                pendentes, new ValidarMesEncerradoService(), mock(GerarLisuraDaFaturaService.class),
                new ValidarAuditoriaConcluidaService());

        FaturaMensalDetalheResponse detalhe = service.montarDetalhe(fatura);

        assertEquals("Clínica X", detalhe.ocsNome());
        assertEquals(2, detalhe.quantidadeEspelhos());
        assertEquals(2, detalhe.quantidadeItens());
        assertEquals(new BigDecimal("160.00"), detalhe.valorApresentado());
        assertEquals(new BigDecimal("150.00"), detalhe.valorContratado());
        assertEquals(1, detalhe.espelhosPendentes());
        assertEquals(9, detalhe.pendentes().getFirst().espelhoId());
        assertFalse(detalhe.podeEncaminhar());
    }
}
