package com.datasphere.fusex.lisura.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemRepository;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.lisura.dto.LisuraItemResponse;
import com.datasphere.fusex.lisura.dto.LisuraResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            ContratoModel contrato = consultarContratoDoItemService.consultarContratoDoItem(item);
            itensAvaliados.add(avaliarAutorizacaoDoItemService.avaliarAutorizacaoDoItem(item, contrato, ocsId));
        }

        return new LisuraResponse(fatura.getId(), itensAvaliados);
    }
}