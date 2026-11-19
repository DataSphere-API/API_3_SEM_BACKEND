package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.contrato.service.CalcularValorContratadoService;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.ItemFaturaModel;
import com.datasphere.fusex.fatura.ItemFaturaRepository;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MontarItensFaturaService {

    private final EspelhoItemRepository espelhoItemRepository;
    private final ItemFaturaRepository itemFaturaRepository;
    private final GuiaProcRepository guiaProcRepository;
    private final CalcularDivergenciaValorService calcularDivergenciaValorService;
    private final CalcularValorContratadoService calcularValorContratadoService;

    public MontarItensFaturaService(EspelhoItemRepository espelhoItemRepository,
                                    ItemFaturaRepository itemFaturaRepository,
                                    GuiaProcRepository guiaProcRepository,
                                    CalcularDivergenciaValorService calcularDivergenciaValorService,
                                    CalcularValorContratadoService calcularValorContratadoService) {
        this.espelhoItemRepository = espelhoItemRepository;
        this.itemFaturaRepository = itemFaturaRepository;
        this.guiaProcRepository = guiaProcRepository;
        this.calcularDivergenciaValorService = calcularDivergenciaValorService;
        this.calcularValorContratadoService = calcularValorContratadoService;
    }

    // Cada item do espelho vira um item da fatura, comparado com o contrato do próprio atendimento.
    public List<ItemFaturaModel> montarItensFatura(EspelhoModel espelho, FaturaModel fatura) {
        List<ItemFaturaModel> itensFatura = new ArrayList<>();
        for (EspelhoItemModel itemEspelho : espelhoItemRepository.findByEspelhoId(espelho.getId())) {
            BigDecimal valorContratado = calcularValorContratadoService.calcular(
                    guiaProcRepository.findById(itemEspelho.getAtendimentoId()).orElse(null));
            if (valorContratado == null) {
                // Sem contrato: a lisura marca o item como não autorizado.
                valorContratado = BigDecimal.ZERO;
            }
            ItemFaturaModel itemFatura = new ItemFaturaModel();
            itemFatura.setFaturaId(fatura.getId());
            itemFatura.setEspelhoItemId(itemEspelho.getId());
            itemFatura.setValorApresentado(itemEspelho.getValor());
            itemFatura.setValorContratado(valorContratado);
            itemFatura.setDivergenciaValor(
                    calcularDivergenciaValorService.calcularDivergenciaValor(itemEspelho.getValor(), valorContratado));
            itensFatura.add(itemFaturaRepository.save(itemFatura));
        }
        return itensFatura;
    }
}
