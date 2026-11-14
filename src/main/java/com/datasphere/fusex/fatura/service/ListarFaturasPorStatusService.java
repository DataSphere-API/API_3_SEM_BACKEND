package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.FaturaRepository;
import com.datasphere.fusex.fatura.ItemFaturaModel;
import com.datasphere.fusex.fatura.ItemFaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.fatura.dto.FaturaResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarFaturasPorStatusService {

    private final FaturaRepository faturaRepository;
    private final ItemFaturaRepository itemFaturaRepository;

    public ListarFaturasPorStatusService(FaturaRepository faturaRepository, ItemFaturaRepository itemFaturaRepository) {
        this.faturaRepository = faturaRepository;
        this.itemFaturaRepository = itemFaturaRepository;
    }

    public List<FaturaResponse> listar(StatusFatura status) {
        List<FaturaModel> faturas = (status != null)
                ? faturaRepository.findByStatus(status)
                : faturaRepository.findAll();

        return faturas.stream().map(fatura -> {
            List<ItemFaturaModel> itens = itemFaturaRepository.findByFaturaId(fatura.getId());
            return FaturaResponse.from(fatura, itens, null);
        }).toList();
    }
}