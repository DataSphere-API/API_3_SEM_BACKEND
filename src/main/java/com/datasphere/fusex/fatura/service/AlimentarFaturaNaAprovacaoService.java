package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemRepository;
import com.datasphere.fusex.fatura.*;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AlimentarFaturaNaAprovacaoService {

    private final FaturaRepository faturaRepository;
    private final ItemFaturaRepository itemFaturaRepository;
    private final EspelhoItemRepository espelhoItemRepository;
    private final GuiaProcRepository guiaProcRepository;
    private final MontarItensFaturaService montarItensFaturaService;
    private final AtualizarStatusGuiaProcParaFaturadaService atualizarStatusGuiaProcParaFaturadaService;

    public AlimentarFaturaNaAprovacaoService(FaturaRepository faturaRepository,
                                             ItemFaturaRepository itemFaturaRepository,
                                             EspelhoItemRepository espelhoItemRepository,
                                             GuiaProcRepository guiaProcRepository,
                                             MontarItensFaturaService montarItensFaturaService,
                                             AtualizarStatusGuiaProcParaFaturadaService atualizarStatusGuiaProcParaFaturadaService) {
        this.faturaRepository = faturaRepository;
        this.itemFaturaRepository = itemFaturaRepository;
        this.espelhoItemRepository = espelhoItemRepository;
        this.guiaProcRepository = guiaProcRepository;
        this.montarItensFaturaService = montarItensFaturaService;
        this.atualizarStatusGuiaProcParaFaturadaService = atualizarStatusGuiaProcParaFaturadaService;
    }

    @Transactional
    public FaturaModel processarAprovacaoEspelho(EspelhoModel espelho) {
        String competencia = espelho.getDataInicio().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String ocsId = espelho.getOcsId();

        FaturaModel fatura = faturaRepository
                .findByOcsIdAndCompetenciaAndStatus(ocsId, competencia, StatusFatura.ATIVA)
                .orElse(null);

        if (fatura != null && fatura.getStatus() == StatusFatura.ENCAMINHADA_APROVACAO) {
            throw new RuntimeException("Fatura já encaminhada não aceita mais espelhos.");
        }

        GuiaProcModel atendimento = guiaProcRepository.findById(espelho.getAtendimentoId()).orElseThrow();

        if (fatura == null) {
            fatura = new FaturaModel();
            fatura.setOcsId(ocsId);
            fatura.setCompetencia(competencia);
            fatura.setEspelhoModel(espelho);
            fatura.setGuiaProcModel(atendimento);
            fatura.setStatus(StatusFatura.ATIVA);
            fatura = faturaRepository.save(fatura);
        }

        montarItensFaturaService.montarItensFatura(espelho, fatura);

        espelho.setStatus(StatusEspelho.FATURADO);
        atualizarStatusGuiaProcParaFaturadaService.atualizarStatusGuiaProcParaFaturada(atendimento);

        return fatura;
    }

    @Transactional
    public void processarReprovacaoEspelho(EspelhoModel espelho) {
        String competencia = espelho.getDataInicio().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        FaturaModel fatura = faturaRepository
                .findByOcsIdAndCompetenciaAndStatus(espelho.getOcsId(), competencia, StatusFatura.ATIVA)
                .orElse(null);

        if (fatura != null && fatura.getStatus() == StatusFatura.ATIVA) {
            List<EspelhoItemModel> itensEspelho = espelhoItemRepository.findByEspelhoId(espelho.getId());
            List<Integer> idsItens = itensEspelho.stream().map(EspelhoItemModel::getId).toList();

            itemFaturaRepository.deleteByFaturaIdAndEspelhoItemIdIn(fatura.getId(), idsItens);

            espelho.setStatus(StatusEspelho.EM_ABERTO);
        }
    }
}