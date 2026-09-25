package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemRepository;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.ItemFaturaModel;
import com.datasphere.fusex.fatura.ItemFaturaRepository;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
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

    public MontarItensFaturaService(EspelhoItemRepository espelhoItemRepository,
                                    ItemFaturaRepository itemFaturaRepository,
                                    GuiaProcRepository guiaProcRepository,
                                    CalcularDivergenciaValorService calcularDivergenciaValorService) {
        this.espelhoItemRepository = espelhoItemRepository;
        this.itemFaturaRepository = itemFaturaRepository;
        this.guiaProcRepository = guiaProcRepository;
        this.calcularDivergenciaValorService = calcularDivergenciaValorService;
    }

    public List<ItemFaturaModel> montarItensFatura(EspelhoModel espelho, FaturaModel fatura) {
        GuiaProcModel atendimento = guiaProcRepository.findById(espelho.getAtendimentoId()).orElseThrow();
        BigDecimal valorContratado = atendimento.getContratoModel().getValor();

        List<EspelhoItemModel> itensEspelho = espelhoItemRepository.findByEspelhoId(espelho.getId());
        List<ItemFaturaModel> itensFatura = new ArrayList<>();

        for (EspelhoItemModel itemEspelho : itensEspelho) {
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