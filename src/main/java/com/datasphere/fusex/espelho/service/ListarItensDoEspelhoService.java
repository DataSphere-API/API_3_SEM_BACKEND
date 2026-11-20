package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.contrato.service.CalcularValorContratadoService;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.espelhoitem.dto.EspelhoItemResponse;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ListarItensDoEspelhoService {

    private final EspelhoItemRepository espelhoItemRepository;
    private final GuiaProcRepository guiaProcRepository;
    private final CalcularValorContratadoService calcularValorContratadoService;

    public ListarItensDoEspelhoService(EspelhoItemRepository espelhoItemRepository,
                                       GuiaProcRepository guiaProcRepository,
                                       CalcularValorContratadoService calcularValorContratadoService) {
        this.espelhoItemRepository = espelhoItemRepository;
        this.guiaProcRepository = guiaProcRepository;
        this.calcularValorContratadoService = calcularValorContratadoService;
    }

    // Cada item vem com o valor do contrato do atendimento, para o auditor e a OCS compararem.
    @Transactional(readOnly = true)
    public List<EspelhoItemResponse> listarItensDoEspelho(Integer espelhoId) {
        List<EspelhoItemResponse> itens = new ArrayList<>();
        for (EspelhoItemModel item : espelhoItemRepository.findByEspelhoId(espelhoId)) {
            var atendimento = guiaProcRepository.findById(item.getAtendimentoId()).orElse(null);
            itens.add(EspelhoItemResponse.from(item, calcularValorContratadoService.calcular(atendimento)));
        }
        return itens;
    }
}
