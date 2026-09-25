package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.request.EspelhoRequest;
import com.datasphere.fusex.espelho.request.EspelhoResponse;
import com.datasphere.fusex.espelho.request.ItemEspelhoRequest;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CriarEspelhoComItensService {

    private final EspelhoRepository espelhoRepository;
    private final EspelhoItemRepository espelhoItemRepository;
    private final VincularItemAoAtendimentoService vincularItemAoAtendimentoService;
    private final VincularItemAoBeneficiarioService vincularItemAoBeneficiarioService;

    public CriarEspelhoComItensService(EspelhoRepository espelhoRepository,
                                       EspelhoItemRepository espelhoItemRepository,
                                       VincularItemAoAtendimentoService vincularItemAoAtendimentoService,
                                       VincularItemAoBeneficiarioService vincularItemAoBeneficiarioService) {
        this.espelhoRepository = espelhoRepository;
        this.espelhoItemRepository = espelhoItemRepository;
        this.vincularItemAoAtendimentoService = vincularItemAoAtendimentoService;
        this.vincularItemAoBeneficiarioService = vincularItemAoBeneficiarioService;
    }

    public EspelhoResponse criarEspelhoComItens(EspelhoRequest request) {
        EspelhoModel espelho = new EspelhoModel();
        espelho.setAtendimentoId(request.atendimentoId());
        espelho.setOcsId(request.ocsId());
        espelho.setDataInicio(LocalDate.now());
        espelho.setDataFim(LocalDate.now());
        espelho.setStatus(StatusEspelho.EM_ABERTO);
        espelho = espelhoRepository.save(espelho);

        List<EspelhoItemModel> itens = new ArrayList<>();
        for (ItemEspelhoRequest itemRequest : request.itens()) {
            EspelhoItemModel item = new EspelhoItemModel();
            item.setEspelhoId(espelho.getId());
            item.setDescricao(itemRequest.descricao());
            item.setValor(itemRequest.valor());

            vincularItemAoAtendimentoService.vincularItemAoAtendimento(item, request.atendimentoId());
            vincularItemAoBeneficiarioService.vincularItemAoBeneficiario(item, itemRequest.beneficiarioId());

            item = espelhoItemRepository.save(item);
            itens.add(item);
        }

        return new EspelhoResponse(espelho.getId(), espelho.getAtendimentoId(), espelho.getStatus(), itens);
    }
}