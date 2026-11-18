package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.preguia.PreGuiaProcRepository;
import com.datasphere.fusex.preguia.StatusPreGuia;
import com.datasphere.fusex.preguia.dto.PreGuiaRequest;
import com.datasphere.fusex.preguia.dto.PreGuiaSalvaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EditarPreGuiaService {

    private final BuscarPreGuiaPorIdService buscarPreGuiaPorIdService;
    private final BuscarUsuarioLogadoService buscarUsuarioLogadoService;
    private final ValidarDadosDaPreGuiaService validarDadosDaPreGuiaService;
    private final PreGuiaProcRepository preGuiaProcRepository;
    private final SalvarProcedimentosDaPreGuiaService salvarProcedimentosDaPreGuiaService;
    private final RegistrarStatusPreGuiaService registrarStatusPreGuiaService;

    public EditarPreGuiaService(BuscarPreGuiaPorIdService buscarPreGuiaPorIdService,
                                BuscarUsuarioLogadoService buscarUsuarioLogadoService,
                                ValidarDadosDaPreGuiaService validarDadosDaPreGuiaService,
                                PreGuiaProcRepository preGuiaProcRepository,
                                SalvarProcedimentosDaPreGuiaService salvarProcedimentosDaPreGuiaService,
                                RegistrarStatusPreGuiaService registrarStatusPreGuiaService) {
        this.buscarPreGuiaPorIdService = buscarPreGuiaPorIdService;
        this.buscarUsuarioLogadoService = buscarUsuarioLogadoService;
        this.validarDadosDaPreGuiaService = validarDadosDaPreGuiaService;
        this.preGuiaProcRepository = preGuiaProcRepository;
        this.salvarProcedimentosDaPreGuiaService = salvarProcedimentosDaPreGuiaService;
        this.registrarStatusPreGuiaService = registrarStatusPreGuiaService;
    }

    @Transactional
    public PreGuiaSalvaResponse editar(Long id, PreGuiaRequest request) {
        var usuario = buscarUsuarioLogadoService.buscarUsuarioLogado();
        if (!usuario.tem(Perfil.EMISSOR) || usuario.analistaId() == null) {
            throw new BusinessException(ErrorCode.ACESSO_NEGADO);
        }
        var preGuia = buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(id);
        if (preGuia.getStatus() != StatusPreGuia.PRE_GUIA
                && preGuia.getStatus() != StatusPreGuia.COM_PENDENCIAS) {
            throw new BusinessException(ErrorCode.PRE_GUIA_STATUS_INVALIDO);
        }
        validarDadosDaPreGuiaService.validar(request);

        preGuiaProcRepository.deleteAll(preGuiaProcRepository.findByPreGuiaModel_IdOrderByIdAsc(id));
        preGuia.setBenfId(request.beneficiarioPrecCp());
        preGuia.setMedId(request.medicoId());
        salvarProcedimentosDaPreGuiaService.salvar(preGuia, request.procedimentos());
        registrarStatusPreGuiaService.atualizarStatus(preGuia, StatusPreGuia.PRE_GUIA, "edicao-pre-guia");
        return new PreGuiaSalvaResponse(preGuia.getId(), preGuia.getStatus());
    }
}
