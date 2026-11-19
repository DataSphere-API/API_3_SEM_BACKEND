package com.datasphere.fusex.lisura.service;

import com.datasphere.fusex.atendimento.SessaoRepository;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.fatura.ItemFaturaRepository;
import com.datasphere.fusex.lisura.dto.LisuraItemResponse;
import com.datasphere.fusex.lisura.dto.LisuraResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GerarLisuraDaFaturaService {

    private final SessaoRepository sessaoRepository;
    private final ItemFaturaRepository itemFaturaRepository;
    private final EspelhoItemRepository espelhoItemRepository;
    private final ConsultarContratoDoItemService consultarContratoDoItemService;
    private final AvaliarAutorizacaoDoItemService avaliarAutorizacaoDoItemService;

    public GerarLisuraDaFaturaService(EspelhoItemRepository espelhoItemRepository,
                                      ConsultarContratoDoItemService consultarContratoDoItemService,
                                      AvaliarAutorizacaoDoItemService avaliarAutorizacaoDoItemService,
                                      SessaoRepository sessaoRepository,
                                      ItemFaturaRepository itemFaturaRepository) {
        this.sessaoRepository = sessaoRepository;
        this.itemFaturaRepository = itemFaturaRepository;
        this.espelhoItemRepository = espelhoItemRepository;
        this.consultarContratoDoItemService = consultarContratoDoItemService;
        this.avaliarAutorizacaoDoItemService = avaliarAutorizacaoDoItemService;
    }

    @Transactional(readOnly = true)
    public LisuraResponse gerarLisuraDaFatura(FaturaModel fatura) {
        EspelhoModel espelho = fatura.getEspelhoModel();

        String ocsId = fatura.getOcsId();
        if (ocsId == null && espelho != null) {
            ocsId = espelho.getOcsId();
        }
        if (ocsId == null && fatura.getGuiaProcModel() != null) {
            ocsId = fatura.getGuiaProcModel().getContratoModel().getOcsModel().getCnpj();
        }

        List<EspelhoItemModel> itensDoEspelho = buscarItensDaFatura(fatura);
        List<LisuraItemResponse> itensAvaliados = new ArrayList<>();

        for (EspelhoItemModel item : itensDoEspelho) {
            GuiaProcModel atendimento = consultarContratoDoItemService.consultarAtendimentoDoItem(item);
            LocalDate dataAtendimento = null;
            if (atendimento != null) {
                LocalDateTime realizadaEm = sessaoRepository.findUltimaDataRealizacaoByGuiaProcId(item.getAtendimentoId())
                        .orElseGet(atendimento::getDataValidacao);
                if (realizadaEm != null) {
                    dataAtendimento = realizadaEm.toLocalDate();
                }
            }
            itensAvaliados.add(avaliarAutorizacaoDoItemService
                    .avaliarAutorizacaoDoItem(item, atendimento, ocsId, dataAtendimento));
        }
        return new LisuraResponse(fatura.getId(), itensAvaliados);
    }

    private List<EspelhoItemModel> buscarItensDaFatura(FaturaModel fatura) {
        var itens = itemFaturaRepository.findByFaturaId(fatura.getId());
        if (!itens.isEmpty()) {
            return itens.stream().map(item -> espelhoItemRepository.findById(item.getEspelhoItemId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Item de espelho da fatura não encontrado: " + item.getEspelhoItemId())))
                    .toList();
        }
        if (fatura.getEspelhoModel() != null) {
            return espelhoItemRepository.findByEspelhoId(fatura.getEspelhoModel().getId());
        }
        return List.of();
    }
}
