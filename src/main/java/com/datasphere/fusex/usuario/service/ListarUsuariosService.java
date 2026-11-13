package com.datasphere.fusex.usuario.service;

import com.datasphere.fusex.chefe.service.ValidarChefeService;
import com.datasphere.fusex.usuario.UsuarioRepository;
import com.datasphere.fusex.usuario.dto.UsuariosResponse;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import java.security.Principal;

@Service
public class ListarUsuariosService {

    private final UsuarioRepository usuarioRepository;
    private final MontarUsuarioResponseService montarUsuarioResponseService;
    private final ValidarChefeService validarChefeService;

    public ListarUsuariosService(UsuarioRepository usuarioRepository,
                                  MontarUsuarioResponseService montarUsuarioResponseService,
                                  ValidarChefeService validarChefeService) {
        this.usuarioRepository = usuarioRepository;
        this.montarUsuarioResponseService = montarUsuarioResponseService;
        this.validarChefeService = validarChefeService;
    }

    @Transactional(readOnly = true)
    public UsuariosResponse listar(Principal principal) {
        validarChefeService.validar(principal);
        return new UsuariosResponse(usuarioRepository.findAll(Sort.by("id")).stream()
                .map(montarUsuarioResponseService::montar).toList());
    }
}
