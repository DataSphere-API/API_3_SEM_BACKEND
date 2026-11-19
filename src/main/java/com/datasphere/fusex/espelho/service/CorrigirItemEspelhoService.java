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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CorrigirItemEspelhoService {

    private final EspelhoItemRepository espelhoItemRepository;
    private final EspelhoRepository espelhoRepository;
    private final BuscarEspelhoPorIdService buscarEspelhoPorIdService;
    private final ValidarBeneficiarioDaGuiaService validarBeneficiarioDaGuiaService;

    public CorrigirItemEspelhoService(EspelhoItemRepository espelhoItemRepository,
                                      EspelhoRepository espelhoRepository,
                                      BuscarEspelhoPorIdService buscarEspelhoPorIdService,
                                      ValidarBeneficiarioDaGuiaService validarBeneficiarioDaGuiaService) {
        this.espelhoItemRepository = espelhoItemRepository;
        this.espelhoRepository = espelhoRepository;
        this.buscarEspelhoPorIdService = buscarEspelhoPorIdService;
        this.validarBeneficiarioDaGuiaService = validarBeneficiarioDaGuiaService;
    }

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

        boolean alterado = false;

        if (dados.beneficiarioId() != null && !dados.beneficiarioId().isBlank()) {
            validarBeneficiarioDaGuiaService.validarBeneficiarioItem(espelho.getAtendimentoId(), dados.beneficiarioId());
            item.setBeneficiarioId(dados.beneficiarioId());
            alterado = true;
        }

        if (dados.descricao() != null && !dados.descricao().isBlank()) {
            item.setDescricao(dados.descricao());
            alterado = true;
        }

        if (dados.valor() != null && dados.valor().compareTo(BigDecimal.ZERO) > 0) {
            item.setValor(dados.valor());
            alterado = true;
        }

        if (alterado) {
            item = espelhoItemRepository.save(item);
            espelho.setDataAtualizacao(LocalDateTime.now());
            espelhoRepository.save(espelho);
        }

        return item;
    }
}
