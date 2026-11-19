package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.FaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.service.RegistrarMudancaStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlimentarFaturaNaAprovacaoServiceTest {

    @Mock private FaturaRepository faturaRepository;
    @Mock private EspelhoRepository espelhoRepository;
    @Mock private EspelhoItemRepository espelhoItemRepository;
    @Mock private GuiaProcRepository guiaProcRepository;
    @Mock private MontarItensFaturaService montarItensFaturaService;
    @Mock private AtualizarStatusGuiaProcParaFaturadaService atualizarStatusGuiaProcParaFaturadaService;
    @Mock private RegistrarMudancaStatusService registrarMudancaStatusService;

    @InjectMocks
    private AlimentarFaturaNaAprovacaoService service;

    private EspelhoModel espelho;
    private GuiaProcModel primeiro;
    private GuiaProcModel segundo;

    @BeforeEach
    void setUp() {
        espelho = new EspelhoModel();
        espelho.setId(5);
        espelho.setOcsId("12345678000199");
        espelho.setAtendimentoId(10L);
        espelho.setDataInicio(LocalDate.of(2026, 11, 1));
        espelho.setStatus(StatusEspelho.APROVADO);
        primeiro = new GuiaProcModel();
        primeiro.setId(10L);
        segundo = new GuiaProcModel();
        segundo.setId(11L);
        when(espelhoItemRepository.findByEspelhoId(5)).thenReturn(List.of(item(10L), item(11L)));
        when(guiaProcRepository.findById(10L)).thenReturn(Optional.of(primeiro));
        when(guiaProcRepository.findById(11L)).thenReturn(Optional.of(segundo));
    }

    private EspelhoItemModel item(Long atendimentoId) {
        EspelhoItemModel item = new EspelhoItemModel();
        item.setEspelhoId(5);
        item.setAtendimentoId(atendimentoId);
        return item;
    }

    @Test
    void primeiroEspelhoDoMes_abreAFaturaEFaturaTudo() {
        when(faturaRepository.findByOcsIdAndCompetenciaAndStatus("12345678000199", "2026-11", StatusFatura.ATIVA))
                .thenReturn(Optional.empty());
        when(faturaRepository.save(any(FaturaModel.class))).thenAnswer(inv -> {
            FaturaModel fatura = inv.getArgument(0);
            fatura.setId(30L);
            return fatura;
        });

        FaturaModel fatura = service.processarAprovacaoEspelho(espelho);

        assertEquals(StatusFatura.ATIVA, fatura.getStatus());
        assertEquals("2026-11", fatura.getCompetencia());
        assertNotNull(fatura.getDataGeracao());
        assertSame(primeiro, fatura.getGuiaProcModel());
        verify(registrarMudancaStatusService).executar(EntidadeTipo.FATURA, 30L, null, "ATIVA", "aprovacao-do-espelho");
        verify(montarItensFaturaService).montarItensFatura(espelho, fatura);
        verify(atualizarStatusGuiaProcParaFaturadaService).atualizarStatusGuiaProcParaFaturada(primeiro);
        verify(atualizarStatusGuiaProcParaFaturadaService).atualizarStatusGuiaProcParaFaturada(segundo);
        assertEquals(StatusEspelho.FATURADO, espelho.getStatus());
        verify(registrarMudancaStatusService).executar(EntidadeTipo.ESPELHO, 5L, "APROVADO", "FATURADO", "inclusao-na-fatura");
    }

    @Test
    void segundoEspelhoDoMes_usaAFaturaAberta() {
        FaturaModel aberta = new FaturaModel();
        aberta.setId(30L);
        aberta.setStatus(StatusFatura.ATIVA);
        when(faturaRepository.findByOcsIdAndCompetenciaAndStatus("12345678000199", "2026-11", StatusFatura.ATIVA))
                .thenReturn(Optional.of(aberta));

        assertSame(aberta, service.processarAprovacaoEspelho(espelho));

        verify(faturaRepository, never()).save(any());
        verify(registrarMudancaStatusService, never()).executar(eq(EntidadeTipo.FATURA), any(), any(), any(), any());
        verify(montarItensFaturaService).montarItensFatura(espelho, aberta);
    }
}
