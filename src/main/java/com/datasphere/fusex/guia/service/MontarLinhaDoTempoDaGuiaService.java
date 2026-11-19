package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.HistoricoStatusModel;
import com.datasphere.fusex.historicoStatus.service.HistoricoStatusResponse;
import com.datasphere.fusex.historicoStatus.service.ListarHistoricoPorEntidadeService;
import com.datasphere.fusex.preguia.PreGuiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MontarLinhaDoTempoDaGuiaService {

    private final GuiaRepository guiaRepository;
    private final PreGuiaRepository preGuiaRepository;
    private final GuiaProcRepository guiaProcRepository;
    private final ListarHistoricoPorEntidadeService listarHistoricoPorEntidadeService;
    private final BuscarUsuarioLogadoService buscarUsuarioLogadoService;

    public MontarLinhaDoTempoDaGuiaService(GuiaRepository guiaRepository,
                                          PreGuiaRepository preGuiaRepository,
                                          GuiaProcRepository guiaProcRepository,
                                          ListarHistoricoPorEntidadeService listarHistoricoPorEntidadeService,
                                          BuscarUsuarioLogadoService buscarUsuarioLogadoService) {
        this.guiaRepository = guiaRepository;
        this.preGuiaRepository = preGuiaRepository;
        this.guiaProcRepository = guiaProcRepository;
        this.listarHistoricoPorEntidadeService = listarHistoricoPorEntidadeService;
        this.buscarUsuarioLogadoService = buscarUsuarioLogadoService;
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
        for (var procedimento : guiaProcRepository.findByGuiaModel_Id(guiaId)) {
            eventos.addAll(listarHistoricoPorEntidadeService.executar(EntidadeTipo.GUIA_PROC, procedimento.getId()));
        }
        return eventos.stream()
                .sorted(Comparator.comparing(HistoricoStatusModel::getData).thenComparing(HistoricoStatusModel::getId))
                .map(HistoricoStatusResponse::from)
                .toList();
    }
}
