package com.datasphere.fusex.auth.service;

import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class BuscarUsuarioLogadoService {

    public UsuarioLogado buscarUsuarioLogado() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof UsuarioLogado usuario)) {
            throw new BusinessException(ErrorCode.NAO_AUTENTICADO);
        }
        return usuario;
    }
}