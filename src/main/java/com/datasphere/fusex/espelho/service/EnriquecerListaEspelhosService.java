package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemRepository;
import com.datasphere.fusex.ocs.OcsModel;
import com.datasphere.fusex.ocs.OcsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EnriquecerListaEspelhosService {

    private final OcsRepository ocsRepository;
    private final EspelhoItemRepository espelhoItemRepository;

    public EnriquecerListaEspelhosService(OcsRepository ocsRepository,
                                          EspelhoItemRepository espelhoItemRepository) {
        this.ocsRepository = ocsRepository;
        this.espelhoItemRepository = espelhoItemRepository;
    }

    public void enriquecer(List<EspelhoModel> espelhos) {

        if (espelhos.isEmpty()) {
            return;
        }

        Map<String, String> nomesPorCnpj = ocsRepository.findAllById(
                espelhos.stream()
                        .map(EspelhoModel::getOcsId)
                        .filter(ocsId -> ocsId != null && !ocsId.isBlank())
                        .collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(
                OcsModel::getCnpj,
                OcsModel::getNome
        ));

        for (EspelhoModel espelho : espelhos) {

            espelho.setOcsNome(
                    nomesPorCnpj.get(espelho.getOcsId())
            );

            List<EspelhoItemModel> itens =
                    espelhoItemRepository.findByEspelhoId(espelho.getId());

            BigDecimal valorTotal = itens.stream()
                    .map(EspelhoItemModel::getValor)
                    .filter(valor -> valor != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            espelho.setQuantidadeItens(itens.size());
            espelho.setValorTotal(valorTotal);
        }
    }
}