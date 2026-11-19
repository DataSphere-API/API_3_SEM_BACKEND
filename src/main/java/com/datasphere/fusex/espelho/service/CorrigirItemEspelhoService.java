package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.EspelhoItemPatchRequest;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    // A OCS só corrige o valor cobrado, com o espelho em aberto ou devolvido pelo auditor.
    @Transactional
    public EspelhoItemModel corrigirItemEspelho(Integer espelhoId, Integer itemId, EspelhoItemPatchRequest dados) {
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
        item.setValor(dados.valor());
        item = espelhoItemRepository.save(item);
        espelho.setDataAtualizacao(LocalDateTime.now());
        espelhoRepository.save(espelho);
        return item;
    }
}
