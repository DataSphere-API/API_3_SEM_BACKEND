package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.ItemFaturaModel;
import com.datasphere.fusex.fatura.request.FaturaResponse;
import com.datasphere.fusex.ocs.OcsModel;
import com.datasphere.fusex.ocs.OcsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MontarFaturaResponseService {

    private final OcsRepository ocsRepository;

    public MontarFaturaResponseService(OcsRepository ocsRepository) {
        this.ocsRepository = ocsRepository;
    }

    public FaturaResponse montar(
            FaturaModel fatura,
            List<ItemFaturaModel> itens) {

        String ocsId = fatura.getEspelhoModel().getOcsId();

        String ocsNome = null;

        if (ocsId != null) {
            ocsNome = ocsRepository.findById(ocsId)
                    .map(OcsModel::getNome)
                    .orElse(null);
        }

        return new FaturaResponse(
                fatura.getId(),
                fatura.getGuiaProcModel().getGuiaId(),
                fatura.getEspelhoModel().getId(),
                ocsId,
                ocsNome,
                itens,
                fatura.getStatus(),
                fatura.getDataGeracao(),
                fatura.getFaturaOrigemId()
        );
    }
}