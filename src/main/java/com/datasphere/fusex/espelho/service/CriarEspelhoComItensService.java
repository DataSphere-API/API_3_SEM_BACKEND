package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.atendimento.service.BuscarOcsAutenticadaService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.EspelhoRequest;
import com.datasphere.fusex.espelho.dto.EspelhoResumoResponse;
import com.datasphere.fusex.espelho.dto.ItemEspelhoRequest;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import com.datasphere.fusex.guiaproc.StatusProcedimento;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.service.RegistrarMudancaStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CriarEspelhoComItensService {

    private final EspelhoRepository espelhoRepository;
    private final EspelhoItemRepository espelhoItemRepository;
    private final GuiaProcRepository guiaProcRepository;
    private final BuscarOcsAutenticadaService buscarOcsAutenticadaService;
    private final ValidarPeriodoDoEspelhoService validarPeriodoDoEspelhoService;
    private final EnriquecerListaEspelhosService enriquecerListaEspelhosService;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public CriarEspelhoComItensService(EspelhoRepository espelhoRepository,
                                       EspelhoItemRepository espelhoItemRepository,
                                       GuiaProcRepository guiaProcRepository,
                                       BuscarOcsAutenticadaService buscarOcsAutenticadaService,
                                       ValidarPeriodoDoEspelhoService validarPeriodoDoEspelhoService,
                                       EnriquecerListaEspelhosService enriquecerListaEspelhosService,
                                       RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.espelhoRepository = espelhoRepository;
        this.espelhoItemRepository = espelhoItemRepository;
        this.guiaProcRepository = guiaProcRepository;
        this.buscarOcsAutenticadaService = buscarOcsAutenticadaService;
        this.validarPeriodoDoEspelhoService = validarPeriodoDoEspelhoService;
        this.enriquecerListaEspelhosService = enriquecerListaEspelhosService;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    @Transactional
    public EspelhoResumoResponse criarEspelhoComItens(EspelhoRequest request) {
        String ocsLogada = buscarOcsAutenticadaService.buscarOcsAutenticada();
        if (request.ocsId() != null && !request.ocsId().isBlank() && !request.ocsId().trim().equals(ocsLogada)) {
            throw new BusinessException(ErrorCode.OCS_DIFERENTE_DO_CONTRATO);
        }
        if (request.itens() == null || request.itens().isEmpty()) {
            throw new BusinessException(ErrorCode.ESPELHO_SEM_ITENS, List.of("itens: escolha pelo menos um atendimento"));
        }
        validarPeriodoDoEspelhoService.validar(request.dataInicio(), request.dataFim(), LocalDate.now());

        List<GuiaProcModel> atendimentos = buscarAtendimentos(request, ocsLogada);
        validarQueNenhumEstaEmOutroEspelho(atendimentos);

        EspelhoModel espelho = new EspelhoModel();
        espelho.setAtendimentoId(atendimentos.get(0).getId());
        espelho.setOcsId(ocsLogada);
        espelho.setDataInicio(request.dataInicio());
        espelho.setDataFim(request.dataFim());
        espelho.setStatus(StatusEspelho.EM_ABERTO);
        espelho = espelhoRepository.save(espelho);

        for (int i = 0; i < atendimentos.size(); i++) {
            GuiaProcModel atendimento = atendimentos.get(i);
            EspelhoItemModel item = new EspelhoItemModel();
            item.setEspelhoId(espelho.getId());
            item.setAtendimentoId(atendimento.getId());
            item.setBeneficiarioId(atendimento.getGuiaModel().getBenfId());
            item.setDescricao(atendimento.getContratoModel().getProcedimentoModel().getNome());
            item.setValor(request.itens().get(i).valor());
            espelhoItemRepository.save(item);
        }

        registrarMudancaStatusService.executar(EntidadeTipo.ESPELHO, espelho.getId().longValue(),
                null, StatusEspelho.EM_ABERTO.name(), "criacao-espelho");
        return enriquecerListaEspelhosService.enriquecer(List.of(espelho)).get(0);
    }

    // Cada atendimento: existe, é desta OCS, está REALIZADA e a baixa final caiu dentro do período.
    private List<GuiaProcModel> buscarAtendimentos(EspelhoRequest request, String ocsLogada) {
        List<GuiaProcModel> atendimentos = new ArrayList<>();
        Set<Long> vistos = new HashSet<>();
        for (ItemEspelhoRequest item : request.itens()) {
            Long id = item.atendimentoId();
            if (!vistos.add(id)) {
                throw new BusinessException(ErrorCode.DADOS_INVALIDOS, List.of("itens: atendimento " + id + " repetido"));
            }
            GuiaProcModel atendimento = guiaProcRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ATENDIMENTO_NAO_ENCONTRADO,
                            List.of("atendimento " + id)));
            if (!ocsLogada.equals(atendimento.getContratoModel().getOcsModel().getCnpj())) {
                throw new BusinessException(ErrorCode.OCS_DIVERGENTE, List.of("atendimento " + id + " é de outra OCS"));
            }
            if (!StatusProcedimento.REALIZADA.name().equals(atendimento.getStatus())) {
                throw new BusinessException(ErrorCode.ATENDIMENTO_NAO_REALIZADO, List.of("atendimento " + id));
            }
            LocalDate realizadoEm = atendimento.getDataValidacao() == null ? null : atendimento.getDataValidacao().toLocalDate();
            if (realizadoEm == null || realizadoEm.isBefore(request.dataInicio()) || realizadoEm.isAfter(request.dataFim())) {
                throw new BusinessException(ErrorCode.FORA_DO_PERIODO, List.of("atendimento " + id));
            }
            atendimentos.add(atendimento);
        }
        return atendimentos;
    }

    // Um atendimento só entra em um espelho; espelho cancelado libera o atendimento.
    private void validarQueNenhumEstaEmOutroEspelho(List<GuiaProcModel> atendimentos) {
        List<Long> ids = atendimentos.stream().map(GuiaProcModel::getId).toList();
        for (EspelhoItemModel itemExistente : espelhoItemRepository.findByAtendimentoIdIn(ids)) {
            EspelhoModel outro = espelhoRepository.findById(itemExistente.getEspelhoId()).orElse(null);
            if (outro != null && outro.getStatus() != StatusEspelho.CANCELADO) {
                throw new BusinessException(ErrorCode.ATENDIMENTO_JA_EM_ESPELHO,
                        List.of("atendimento " + itemExistente.getAtendimentoId() + " já está no espelho #" + outro.getId()));
            }
        }
    }
}
