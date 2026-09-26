package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.request.ItemEspelhoRequest;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CorrigirItemEspelhoService {

    private final EspelhoItemRepository espelhoItemRepository;
    private final EspelhoRepository espelhoRepository;
    private final BuscarEspelhoPorIdService buscarEspelhoPorIdService;

    public CorrigirItemEspelhoService(EspelhoItemRepository espelhoItemRepository,
                                      EspelhoRepository espelhoRepository,
                                      BuscarEspelhoPorIdService buscarEspelhoPorIdService) {
        this.espelhoItemRepository = espelhoItemRepository;
        this.espelhoRepository = espelhoRepository;
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

        EspelhoItemModel salvo = espelhoItemRepository.save(item);

        espelho.setDataAtualizacao(LocalDateTime.now());
        espelhoRepository.save(espelho);

        return salvo;
    }
}