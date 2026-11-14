package com.datasphere.fusex.auth.service;

import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.auth.dto.LoginRequest;
import com.datasphere.fusex.auth.dto.LoginResponse;
import com.datasphere.fusex.auth.dto.UsuarioResponse;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.usuario.UsuarioModel;
import com.datasphere.fusex.usuario.UsuarioRepository;
import com.datasphere.fusex.util.Documentos;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final MontarUsuarioLogadoService montarUsuarioLogadoService;
    private final TokenService tokenService;

    public LoginService(UsuarioRepository usuarioRepository,
                        PasswordEncoder passwordEncoder,
                        MontarUsuarioLogadoService montarUsuarioLogadoService,
                        TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.montarUsuarioLogadoService = montarUsuarioLogadoService;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse entrar(LoginRequest request) {
        String cpf = Documentos.somenteDigitos(request.cpf());
        UsuarioModel usuario = null;
        if (!cpf.isEmpty() && request.senha() != null) {
            usuario = usuarioRepository.findByLogin(cpf).orElse(null);
        }

        // CPF inexistente e senha errada devolvem o mesmo erro, para não revelar quem tem cadastro.
        if (usuario == null || !passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new BusinessException(ErrorCode.CREDENCIAIS_INVALIDAS);
        }

        UsuarioLogado logado = montarUsuarioLogadoService.montar(usuario);
        return new LoginResponse(tokenService.gerar(logado), UsuarioResponse.de(logado));
    }
}