package com.datasphere.fusex.lisura.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemRepository;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.lisura.dto.LisuraItemResponse;
import com.datasphere.fusex.lisura.dto.LisuraResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class GerarLisuraDaFaturaService {

    private final EspelhoItemRepository espelhoItemRepository;
    private final ConsultarContratoDoItemService consultarContratoDoItemService;
    private final AvaliarAutorizacaoDoItemService avaliarAutorizacaoDoItemService;

    public GerarLisuraDaFaturaService(EspelhoItemRepository espelhoItemRepository,
                                      ConsultarContratoDoItemService consultarContratoDoItemService,
                                      AvaliarAutorizacaoDoItemService avaliarAutorizacaoDoItemService) {
        this.espelhoItemRepository = espelhoItemRepository;
        this.consultarContratoDoItemService = consultarContratoDoItemService;
        this.avaliarAutorizacaoDoItemService = avaliarAutorizacaoDoItemService;
    }

    @Transactional(readOnly = true)
    public LisuraResponse gerarLisuraDaFatura(FaturaModel fatura) {
        EspelhoModel espelho = fatura.getEspelhoModel();

        String ocsId = espelho.getOcsId();
        if (ocsId == null) {
            ocsId = fatura.getGuiaProcModel().getContratoModel().getOcsModel().getCnpj();
        }

        List<EspelhoItemModel> itensDoEspelho = espelhoItemRepository.findByEspelhoId(espelho.getId());
        List<LisuraItemResponse> itensAvaliados = new ArrayList<>();

        for (EspelhoItemModel item : itensDoEspelho) {
            GuiaProcModel atendimento = consultarContratoDoItemService.consultarAtendimentoDoItem(item);

            ContratoModel contrato = null;
            LocalDate dataValidade = null;
            if (atendimento != null) {
                contrato = atendimento.getContratoModel();
                dataValidade = atendimento.getDataValidade();
            }

            itensAvaliados.add(avaliarAutorizacaoDoItemService
                    .avaliarAutorizacaoDoItem(item, contrato, ocsId, dataValidade));
        }

        return new LisuraResponse(fatura.getId(), itensAvaliados);
    }
}