package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarItensDoEspelhoService {

    private final EspelhoItemRepository espelhoItemRepository;

    public ListarItensDoEspelhoService(EspelhoItemRepository espelhoItemRepository) {
        this.espelhoItemRepository = espelhoItemRepository;
    }

    public List<EspelhoItemModel> listarItensDoEspelho(Integer espelhoId) {
        return espelhoItemRepository.findByEspelhoId(espelhoId);
    }
}