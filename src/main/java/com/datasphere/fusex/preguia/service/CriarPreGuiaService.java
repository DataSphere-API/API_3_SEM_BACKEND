package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaRepository;
import com.datasphere.fusex.preguia.StatusPreGuia;
import com.datasphere.fusex.preguia.dto.PreGuiaRequest;
import com.datasphere.fusex.preguia.dto.PreGuiaSalvaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class CriarPreGuiaService {

    private final PreGuiaRepository preGuiaRepository;
    private final BuscarUsuarioLogadoService buscarUsuarioLogadoService;
    private final ValidarDadosDaPreGuiaService validarDadosDaPreGuiaService;
    private final SalvarProcedimentosDaPreGuiaService salvarProcedimentosDaPreGuiaService;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public CriarPreGuiaService(PreGuiaRepository preGuiaRepository,
                               BuscarUsuarioLogadoService buscarUsuarioLogadoService,
                               ValidarDadosDaPreGuiaService validarDadosDaPreGuiaService,
                               SalvarProcedimentosDaPreGuiaService salvarProcedimentosDaPreGuiaService,
                               RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.preGuiaRepository = preGuiaRepository;
        this.buscarUsuarioLogadoService = buscarUsuarioLogadoService;
        this.validarDadosDaPreGuiaService = validarDadosDaPreGuiaService;
        this.salvarProcedimentosDaPreGuiaService = salvarProcedimentosDaPreGuiaService;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    @Transactional
    public PreGuiaSalvaResponse criar(PreGuiaRequest request) {
        var usuario = buscarUsuarioLogadoService.buscarUsuarioLogado();
        if (!usuario.tem(Perfil.EMISSOR) || usuario.analistaId() == null) {
            throw new BusinessException(ErrorCode.ACESSO_NEGADO);
        }
        validarDadosDaPreGuiaService.validar(request);

        var preGuia = new PreGuiaModel();
        preGuia.setBenfId(request.beneficiarioPrecCp());
        preGuia.setMedId(request.medicoId());
        preGuia.setEmissorId(usuario.analistaId());
        preGuia.setStatus(StatusPreGuia.PRE_GUIA);
        preGuia.setDataCriacao(LocalDate.now());
        preGuiaRepository.save(preGuia);
        salvarProcedimentosDaPreGuiaService.salvar(preGuia, request.procedimentos());
        registrarMudancaStatusService.executar(EntidadeTipo.PRE_GUIA, preGuia.getId(),
                null, StatusPreGuia.PRE_GUIA.name(), "criacao-pre-guia");
        return new PreGuiaSalvaResponse(preGuia.getId(), preGuia.getStatus());
    }
}
