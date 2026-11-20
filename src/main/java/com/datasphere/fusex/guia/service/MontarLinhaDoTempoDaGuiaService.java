package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.fatura.ItemFaturaModel;
import com.datasphere.fusex.fatura.ItemFaturaRepository;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.HistoricoStatusModel;
import com.datasphere.fusex.historicostatus.dto.HistoricoStatusResponse;
import com.datasphere.fusex.historicostatus.service.ListarHistoricoPorEntidadeService;
import com.datasphere.fusex.preguia.PreGuiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class MontarLinhaDoTempoDaGuiaService {

    private final GuiaRepository guiaRepository;
    private final PreGuiaRepository preGuiaRepository;
    private final GuiaProcRepository guiaProcRepository;
    private final ListarHistoricoPorEntidadeService listarHistoricoPorEntidadeService;
    private final BuscarUsuarioLogadoService buscarUsuarioLogadoService;
    private final EspelhoItemRepository espelhoItemRepository;
    private final ItemFaturaRepository itemFaturaRepository;

    public MontarLinhaDoTempoDaGuiaService(GuiaRepository guiaRepository,
                                          PreGuiaRepository preGuiaRepository,
                                          GuiaProcRepository guiaProcRepository,
                                          ListarHistoricoPorEntidadeService listarHistoricoPorEntidadeService,
                                          BuscarUsuarioLogadoService buscarUsuarioLogadoService,
                                          EspelhoItemRepository espelhoItemRepository,
                                          ItemFaturaRepository itemFaturaRepository) {
        this.guiaRepository = guiaRepository;
        this.preGuiaRepository = preGuiaRepository;
        this.guiaProcRepository = guiaProcRepository;
        this.listarHistoricoPorEntidadeService = listarHistoricoPorEntidadeService;
        this.buscarUsuarioLogadoService = buscarUsuarioLogadoService;
        this.espelhoItemRepository = espelhoItemRepository;
        this.itemFaturaRepository = itemFaturaRepository;
    }

    @Transactional(readOnly = true)
    public List<HistoricoStatusResponse> montar(Long guiaId) {
        var usuario = buscarUsuarioLogadoService.buscarUsuarioLogado();
        boolean consultaAdministrativa = usuario.tem(Perfil.AUDITOR)
                || usuario.tem(Perfil.EMISSOR) || usuario.tem(Perfil.CHEFE);
        if (!consultaAdministrativa && !usuario.tem(Perfil.BENEFICIARIO)) {
            throw new BusinessException(ErrorCode.ACESSO_NEGADO);
        }
        var guia = guiaRepository.findById(guiaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GUIA_NAO_ENCONTRADA));
        if (!consultaAdministrativa
                && (usuario.precCp() == null || !usuario.precCp().equals(guia.getBenfId()))) {
            throw new BusinessException(ErrorCode.BENEFICIARIO_SEM_PERMISSAO);
        }

        List<HistoricoStatusModel> eventos = new ArrayList<>();
        preGuiaRepository.findByGuiaId(guiaId).ifPresent(preGuia ->
                eventos.addAll(listarHistoricoPorEntidadeService.executar(EntidadeTipo.PRE_GUIA, preGuia.getId())));
        eventos.addAll(listarHistoricoPorEntidadeService.executar(EntidadeTipo.GUIA, guiaId));
        List<Long> atendimentos = new ArrayList<>();
        for (var procedimento : guiaProcRepository.findByGuiaModel_Id(guiaId)) {
            atendimentos.add(procedimento.getId());
            eventos.addAll(listarHistoricoPorEntidadeService.executar(EntidadeTipo.GUIA_PROC, procedimento.getId()));
        }
        eventos.addAll(eventosDosEspelhosEFaturas(atendimentos));
        return eventos.stream()
                .sorted(Comparator.comparing(HistoricoStatusModel::getData).thenComparing(HistoricoStatusModel::getId))
                .map(HistoricoStatusResponse::from)
                .toList();
    }

    // Espelhos em que os procedimentos da guia entraram e as faturas desses itens.
    private List<HistoricoStatusModel> eventosDosEspelhosEFaturas(List<Long> atendimentos) {
        List<HistoricoStatusModel> eventos = new ArrayList<>();
        if (atendimentos.isEmpty()) {
            return eventos;
        }
        List<EspelhoItemModel> itens = espelhoItemRepository.findByAtendimentoIdIn(atendimentos);
        if (itens.isEmpty()) {
            return eventos;
        }
        Set<Integer> espelhos = new LinkedHashSet<>();
        for (EspelhoItemModel item : itens) {
            espelhos.add(item.getEspelhoId());
        }
        for (Integer espelhoId : espelhos) {
            eventos.addAll(listarHistoricoPorEntidadeService.executar(EntidadeTipo.ESPELHO, espelhoId.longValue()));
        }
        Set<Long> faturas = new LinkedHashSet<>();
        for (ItemFaturaModel itemDaFatura : itemFaturaRepository.findByEspelhoItemIdIn(
                itens.stream().map(EspelhoItemModel::getId).toList())) {
            faturas.add(itemDaFatura.getFaturaId());
        }
        for (Long faturaId : faturas) {
            eventos.addAll(listarHistoricoPorEntidadeService.executar(EntidadeTipo.FATURA, faturaId));
        }
        return eventos;
    }
}
