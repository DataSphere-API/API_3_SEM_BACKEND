package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemRepository;
import com.datasphere.fusex.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    @Transactional
    public boolean removerItemDoEspelho(Integer espelhoId, Integer itemId) {
        EspelhoModel espelho = buscarEspelhoPorIdService.buscarEspelhoPorId(espelhoId);
        if (espelho == null) {
            throw new BusinessException(ErrorCode.ESPELHO_NAO_ENCONTRADO);
        }
        if (espelho.getStatus() == StatusEspelho.CANCELADO) {
            throw new BusinessException(ErrorCode.ESPELHO_CANCELADO);
        }
        if (espelho.getStatus() != StatusEspelho.EM_ABERTO && espelho.getStatus() != StatusEspelho.REPROVADO) {
            throw new BusinessException(ErrorCode.ESPELHO_NAO_EDITAVEL);
        }

        EspelhoItemModel item = espelhoItemRepository.findByIdAndEspelhoId(itemId, espelhoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NAO_ENCONTRADO));

        long totalItens = espelhoItemRepository.countByEspelhoId(espelhoId);
        if (totalItens <= 1) {
            throw new BusinessException(ErrorCode.ESPELHO_SEM_ITENS, List.of("itens: não é permitido remover o último item do espelho"));
        }

        espelhoItemRepository.delete(item);

        espelho.setDataAtualizacao(LocalDateTime.now());
        espelhoRepository.save(espelho);

        return true;
    }
}
