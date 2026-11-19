package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.EspelhoRequest;
import com.datasphere.fusex.espelho.dto.EspelhoResponse;
import com.datasphere.fusex.espelho.dto.ItemEspelhoRequest;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.service.RegistrarMudancaStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CriarEspelhoComItensService {

    private final EspelhoRepository espelhoRepository;
    private final EspelhoItemRepository espelhoItemRepository;
    private final VincularItemAoAtendimentoService vincularItemAoAtendimentoService;
    private final VincularItemAoBeneficiarioService vincularItemAoBeneficiarioService;
    private final ValidarAtendimentoRealizadoService validarAtendimentoRealizadoService;
    private final ValidarEspelhoNaoDuplicadoPorAtendimentoService validarEspelhoNaoDuplicadoPorAtendimentoService;
    private final ValidarOcsDoAtendimentoService validarOcsDoAtendimentoService;
    private final ValidarBeneficiarioDaGuiaService validarBeneficiarioDaGuiaService;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public CriarEspelhoComItensService(EspelhoRepository espelhoRepository,
                                       EspelhoItemRepository espelhoItemRepository,
                                       VincularItemAoAtendimentoService vincularItemAoAtendimentoService,
                                       VincularItemAoBeneficiarioService vincularItemAoBeneficiarioService,
                                       ValidarAtendimentoRealizadoService validarAtendimentoRealizadoService,
                                       ValidarEspelhoNaoDuplicadoPorAtendimentoService validarEspelhoNaoDuplicadoPorAtendimentoService,
                                       ValidarOcsDoAtendimentoService validarOcsDoAtendimentoService,
                                       ValidarBeneficiarioDaGuiaService validarBeneficiarioDaGuiaService,
                                       RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.espelhoRepository = espelhoRepository;
        this.espelhoItemRepository = espelhoItemRepository;
        this.vincularItemAoAtendimentoService = vincularItemAoAtendimentoService;
        this.vincularItemAoBeneficiarioService = vincularItemAoBeneficiarioService;
        this.validarAtendimentoRealizadoService = validarAtendimentoRealizadoService;
        this.validarEspelhoNaoDuplicadoPorAtendimentoService = validarEspelhoNaoDuplicadoPorAtendimentoService;
        this.validarOcsDoAtendimentoService = validarOcsDoAtendimentoService;
        this.validarBeneficiarioDaGuiaService = validarBeneficiarioDaGuiaService;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    @Transactional
    public EspelhoResponse criarEspelhoComItens(EspelhoRequest request) {
        if (request.itens() == null || request.itens().isEmpty()) {
            throw new BusinessException(ErrorCode.ESPELHO_SEM_ITENS, List.of("itens: o espelho deve conter pelo menos um item"));
        }

        GuiaProcModel atendimento = validarAtendimentoRealizadoService.validarAtendimentoRealizado(request.atendimentoId());
        if (atendimento == null) {
            throw new BusinessException(ErrorCode.ATENDIMENTO_NAO_REALIZADO);
        }

        validarOcsDoAtendimentoService.validarOcs(request.ocsId(), atendimento);

        boolean naoDuplicado = validarEspelhoNaoDuplicadoPorAtendimentoService.validarEspelhoNaoDuplicadoPorAtendimento(request.atendimentoId());
        if (!naoDuplicado) {
            throw new BusinessException(ErrorCode.ATENDIMENTO_JA_EM_ESPELHO);
        }

        validarBeneficiarioDaGuiaService.validarBeneficiarios(request.atendimentoId(), request.itens());

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

        registrarMudancaStatusService.executar(
                EntidadeTipo.ESPELHO,
                espelho.getId().longValue(),
                null,
                StatusEspelho.EM_ABERTO.name(),
                "criacao-espelho"
        );

        return EspelhoResponse.from(espelho, itens);
    }
}
