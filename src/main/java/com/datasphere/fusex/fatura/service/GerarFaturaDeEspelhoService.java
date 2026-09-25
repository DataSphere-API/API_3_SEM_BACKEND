package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.FaturaRepository;
import com.datasphere.fusex.fatura.ItemFaturaModel;
import com.datasphere.fusex.fatura.request.FaturaResponse;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GerarFaturaDeEspelhoService {

    private final FaturaRepository faturaRepository;
    private final EspelhoRepository espelhoRepository;
    private final GuiaProcRepository guiaProcRepository;
    private final MontarItensFaturaService montarItensFaturaService;
    private final AtualizarStatusGuiaProcParaFaturadaService atualizarStatusGuiaProcParaFaturadaService;

    public GerarFaturaDeEspelhoService(FaturaRepository faturaRepository,
                                       EspelhoRepository espelhoRepository,
                                       GuiaProcRepository guiaProcRepository,
                                       MontarItensFaturaService montarItensFaturaService,
                                       AtualizarStatusGuiaProcParaFaturadaService atualizarStatusGuiaProcParaFaturadaService) {
        this.faturaRepository = faturaRepository;
        this.espelhoRepository = espelhoRepository;
        this.guiaProcRepository = guiaProcRepository;
        this.montarItensFaturaService = montarItensFaturaService;
        this.atualizarStatusGuiaProcParaFaturadaService = atualizarStatusGuiaProcParaFaturadaService;
    }

    public FaturaResponse gerarFaturaDeEspelho(EspelhoModel espelho) {
        GuiaProcModel atendimento = guiaProcRepository.findById(espelho.getAtendimentoId()).orElseThrow();

        FaturaModel fatura = new FaturaModel();
        fatura.setGuiaProcModel(atendimento);
        fatura.setEspelhoModel(espelho);
        fatura = faturaRepository.save(fatura);

        List<ItemFaturaModel> itens = montarItensFaturaService.montarItensFatura(espelho, fatura);

        espelho.setStatus(StatusEspelho.FATURADO);
        espelhoRepository.save(espelho);

        atualizarStatusGuiaProcParaFaturadaService.atualizarStatusGuiaProcParaFaturada(atendimento);

        return new FaturaResponse(fatura.getId(), espelho.getAtendimentoId(), espelho.getId(), itens);
    }
}