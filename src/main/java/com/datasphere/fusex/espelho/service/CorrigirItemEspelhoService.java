package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.request.ItemEspelhoRequest;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CorrigirItemEspelhoService {

    private final EspelhoItemRepository espelhoItemRepository;
    private final BuscarEspelhoPorIdService buscarEspelhoPorIdService;

    public CorrigirItemEspelhoService(EspelhoItemRepository espelhoItemRepository,
                                      BuscarEspelhoPorIdService buscarEspelhoPorIdService) {
        this.espelhoItemRepository = espelhoItemRepository;
        this.buscarEspelhoPorIdService = buscarEspelhoPorIdService;
    }

    public EspelhoItemModel corrigirItemEspelho(Integer espelhoId, Integer itemId, ItemEspelhoRequest dados) {
        EspelhoModel espelho = buscarEspelhoPorIdService.buscarEspelhoPorId(espelhoId);
        if (espelho == null || espelho.getStatus() != StatusEspelho.EM_ABERTO) {
            return null;
        }

        Optional<EspelhoItemModel> itemOptional = espelhoItemRepository.findByIdAndEspelhoId(itemId, espelhoId);
        if (itemOptional.isEmpty()) {
            return null;
        }

        EspelhoItemModel item = itemOptional.get();
        item.setDescricao(dados.descricao());
        item.setValor(dados.valor());
        item.setBeneficiarioId(dados.beneficiarioId());

        return espelhoItemRepository.save(item);
    }
}