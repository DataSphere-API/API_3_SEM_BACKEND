package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.StatusPreGuia;
import com.datasphere.fusex.preguia.dto.ValidacaoPreGuiaResponse;
import com.datasphere.fusex.preguia.dto.ValidarPreGuiaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValidarPreGuiaService {

    private final BuscarPreGuiaPorIdService buscarPreGuiaPorIdService;
    private final PreGuiaValidacaoService preGuiaValidacaoService;
    private final RegistrarStatusPreGuiaService registrarStatusPreGuiaService;

    public ValidarPreGuiaService(BuscarPreGuiaPorIdService buscarPreGuiaPorIdService,
                                 PreGuiaValidacaoService preGuiaValidacaoService,
                                 RegistrarStatusPreGuiaService registrarStatusPreGuiaService) {
        this.buscarPreGuiaPorIdService = buscarPreGuiaPorIdService;
        this.preGuiaValidacaoService = preGuiaValidacaoService;
        this.registrarStatusPreGuiaService = registrarStatusPreGuiaService;
    }

    @Transactional
    public ValidarPreGuiaResponse validarPreGuia(Long id) {
        PreGuiaModel preGuia = buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(id);

        boolean podeValidar = preGuia.getStatus() == StatusPreGuia.PRE_GUIA
                || preGuia.getStatus() == StatusPreGuia.COM_PENDENCIAS;
        if (!podeValidar) {
            throw new BusinessException(ErrorCode.PRE_GUIA_STATUS_INVALIDO);
        }

        ValidacaoPreGuiaResponse validacao = preGuiaValidacaoService.consolidarPendencias(preGuia);

        StatusPreGuia novoStatus = StatusPreGuia.COM_PENDENCIAS;
        if (validacao.validada()) {
            novoStatus = StatusPreGuia.VALIDADA;
        }

        PreGuiaModel salva = registrarStatusPreGuiaService.atualizarStatus(preGuia, novoStatus, "validacao-pre-guia");

        return new ValidarPreGuiaResponse(salva.getId(), salva.getStatus(), validacao.validada(), validacao.pendencias());
    }
}