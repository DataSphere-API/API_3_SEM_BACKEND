package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.preguia.PreGuiaModel;
import org.springframework.stereotype.Service;

@Service
public class IdentificarAprovadorService {

    public enum Papel {
        MEDICO,
        CHEFE
    }

    public record Aprovador(Papel papel, Integer id, Integer usuarioId, String nome) {}

    private final BuscarUsuarioLogadoService buscarUsuarioLogadoService;

    public IdentificarAprovadorService(BuscarUsuarioLogadoService buscarUsuarioLogadoService) {
        this.buscarUsuarioLogadoService = buscarUsuarioLogadoService;
    }

    // Quem é médico responsável e chefe ao mesmo tempo assina como médico.
    public Aprovador identificar(PreGuiaModel preGuia) {
        UsuarioLogado usuario = buscarUsuarioLogadoService.buscarUsuarioLogado();

        boolean medicoResponsavel = usuario.tem(Perfil.MEDICO)
                && usuario.medicoId() != null
                && usuario.medicoId().equals(preGuia.getMedId());
        if (medicoResponsavel) {
            return new Aprovador(Papel.MEDICO, usuario.medicoId(), usuario.id(), usuario.nome());
        }

        if (usuario.tem(Perfil.CHEFE)) {
            return new Aprovador(Papel.CHEFE, usuario.chefeId(), usuario.id(), usuario.nome());
        }

        if (usuario.tem(Perfil.MEDICO)) {
            throw new BusinessException(ErrorCode.MEDICO_NAO_RESPONSAVEL);
        }
        throw new BusinessException(ErrorCode.PERFIL_SEM_PERMISSAO);
    }
}
