package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.contrato.service.CalcularValorContratadoService;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.ItemFaturaModel;
import com.datasphere.fusex.fatura.ItemFaturaRepository;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MontarItensFaturaServiceTest {

    private final EspelhoItemRepository espelhoItens = mock(EspelhoItemRepository.class);
    private final ItemFaturaRepository itensFatura = mock(ItemFaturaRepository.class);
    private final GuiaProcRepository guiaProcs = mock(GuiaProcRepository.class);
    private final MontarItensFaturaService service = new MontarItensFaturaService(espelhoItens, itensFatura, guiaProcs,
            new CalcularDivergenciaValorService(), new CalcularValorContratadoService());

    private EspelhoItemModel item(int id, long atendimentoId, String valor) {
        EspelhoItemModel item = new EspelhoItemModel();
        item.setId(id);
        item.setEspelhoId(5);
        item.setAtendimentoId(atendimentoId);
        item.setValor(new BigDecimal(valor));
        return item;
    }

    private GuiaProcModel atendimento(String valorDoContrato, int sessoes) {
        ContratoModel contrato = new ContratoModel();
        contrato.setValor(new BigDecimal(valorDoContrato));
        GuiaProcModel atendimento = new GuiaProcModel();
        atendimento.setContratoModel(contrato);
        atendimento.setQtdAutorizada(sessoes);
        return atendimento;
    }

    @Test
    void cadaItemUsaOContratoDoProprioAtendimentoVezesAsSessoes() {
        EspelhoModel espelho = new EspelhoModel();
        espelho.setId(5);
        FaturaModel fatura = new FaturaModel();
        fatura.setId(30L);
        when(espelhoItens.findByEspelhoId(5)).thenReturn(List.of(item(1, 10L, "850.00"), item(2, 11L, "90.00")));
        when(guiaProcs.findById(10L)).thenReturn(Optional.of(atendimento("80.00", 10)));
        when(guiaProcs.findById(11L)).thenReturn(Optional.of(atendimento("100.00", 1)));
        when(itensFatura.save(any(ItemFaturaModel.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ItemFaturaModel> itens = service.montarItensFatura(espelho, fatura);

        assertEquals(0, new BigDecimal("800.00").compareTo(itens.get(0).getValorContratado()));
        assertEquals(0, new BigDecimal("50.00").compareTo(itens.get(0).getDivergenciaValor()));
        assertEquals(0, new BigDecimal("100.00").compareTo(itens.get(1).getValorContratado()));
        assertEquals(0, new BigDecimal("-10.00").compareTo(itens.get(1).getDivergenciaValor()));
        assertEquals(30L, itens.get(1).getFaturaId());
    }

    @Test
    void atendimentoSemContratoEntraComContratadoZero() {
        EspelhoModel espelho = new EspelhoModel();
        espelho.setId(5);
        FaturaModel fatura = new FaturaModel();
        fatura.setId(30L);
        when(espelhoItens.findByEspelhoId(5)).thenReturn(List.of(item(1, 10L, "50.00")));
        when(guiaProcs.findById(10L)).thenReturn(Optional.of(new GuiaProcModel()));
        when(itensFatura.save(any(ItemFaturaModel.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ItemFaturaModel> itens = service.montarItensFatura(espelho, fatura);

        assertEquals(0, BigDecimal.ZERO.compareTo(itens.get(0).getValorContratado()));
    }
}
