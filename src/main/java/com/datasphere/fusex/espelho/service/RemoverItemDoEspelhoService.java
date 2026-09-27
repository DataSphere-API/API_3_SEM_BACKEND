package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RemoverItemDoEspelhoService {

    private final EspelhoItemRepository espelhoItemRepository;
    private final EspelhoRepository espelhoRepository;
    private final BuscarEspelhoPorIdService buscarEspelhoPorIdService;

    public RemoverItemDoEspelhoService(EspelhoItemRepository espelhoItemRepository,
                                       EspelhoRepository espelhoRepository,
                                       BuscarEspelhoPorIdService buscarEspelhoPorIdService) {
        this.espelhoItemRepository = espelhoItemRepository;
        this.espelhoRepository = espelhoRepository;
        this.buscarEspelhoPorIdService = buscarEspelhoPorIdService;
    }

    public boolean removerItemDoEspelho(Integer espelhoId, Integer itemId) {
        EspelhoModel espelho = buscarEspelhoPorIdService.buscarEspelhoPorId(espelhoId);
        if (espelho == null || espelho.getStatus() == StatusEspelho.CANCELADO) {
            return false;
        }

        Optional<EspelhoItemModel> itemOptional = espelhoItemRepository.findByIdAndEspelhoId(itemId, espelhoId);
        if (itemOptional.isEmpty()) {
            return false;
        }

        espelhoItemRepository.delete(itemOptional.get());

        espelho.setDataAtualizacao(LocalDateTime.now());
        espelhoRepository.save(espelho);

        return true;
    }
}