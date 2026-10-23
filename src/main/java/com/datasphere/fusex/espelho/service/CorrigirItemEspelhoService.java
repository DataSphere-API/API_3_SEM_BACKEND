package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.EspelhoItemPatchRequest;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public EspelhoItemModel corrigirItemEspelho(Integer espelhoId, Integer itemId, EspelhoItemPatchRequest dados) {
        EspelhoModel espelho = buscarEspelhoPorIdService.buscarEspelhoPorId(espelhoId);
        if (espelho == null || espelho.getStatus() == StatusEspelho.CANCELADO) {
            return null;
        }

        Optional<EspelhoItemModel> itemOptional = espelhoItemRepository.findByIdAndEspelhoId(itemId, espelhoId);
        if (itemOptional.isEmpty()) {
            return null;
        }

        EspelhoItemModel item = itemOptional.get();
        boolean alterado = false;

        if (dados.descricao() != null && !dados.descricao().isBlank()) {
            item.setDescricao(dados.descricao());
            alterado = true;
        }

        if (dados.beneficiarioId() != null && !dados.beneficiarioId().isBlank()) {
            item.setBeneficiarioId(dados.beneficiarioId());
            alterado = true;
        }

        if (dados.valor() != null && dados.valor().compareTo(BigDecimal.ZERO) > 0) {
            item.setValor(dados.valor());
            alterado = true;
        }

        if (!alterado) {
            return null;
        }

        EspelhoItemModel salvo = espelhoItemRepository.save(item);

        espelho.setDataAtualizacao(LocalDateTime.now());
        espelhoRepository.save(espelho);

        return salvo;
    }
}