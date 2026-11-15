package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import org.springframework.stereotype.Service;

@Service
public class BuscarOcsAutenticadaService {

    private final BuscarUsuarioLogadoService buscarUsuarioLogadoService;

    public BuscarOcsAutenticadaService(BuscarUsuarioLogadoService buscarUsuarioLogadoService) {
        this.buscarUsuarioLogadoService = buscarUsuarioLogadoService;
    }

    public String buscarOcsAutenticada() {
        var usuario = buscarUsuarioLogadoService.buscarUsuarioLogado();
        if (!usuario.tem(Perfil.OCS) || usuario.ocsCnpj() == null || usuario.ocsCnpj().isBlank()) {
            throw new BusinessException(ErrorCode.ACESSO_NEGADO);
        }
        return usuario.ocsCnpj();
    }
}
