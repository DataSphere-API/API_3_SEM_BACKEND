package com.datasphere.fusex.auth.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.auth.dto.LoginRequest;
import com.datasphere.fusex.auth.dto.LoginResponse;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.usuario.UsuarioModel;
import com.datasphere.fusex.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MontarUsuarioLogadoService montarUsuarioLogadoService;
    @Mock
    private TokenService tokenService;

    @InjectMocks
    private LoginService service;

    private UsuarioModel usuario() {
        UsuarioModel u = new UsuarioModel();
        u.setId(2);
        u.setLogin("08301661305");
        u.setSenha("$2a$hash");
        return u;
    }

    @Test
    void cpfComMascaraESenhaCerta_devolveTokenEUsuario() {
        UsuarioLogado logado = new UsuarioLogado(2, "08301661305", "Carlos Medeiros",
                EnumSet.of(Perfil.MEDICO, Perfil.BENEFICIARIO), "222222222-00",
                1, "SP123456", null, null, null, null, null);
        when(usuarioRepository.findByLogin("08301661305")).thenReturn(Optional.of(usuario()));
        when(passwordEncoder.matches("fusex123", "$2a$hash")).thenReturn(true);
        when(montarUsuarioLogadoService.montar(any(UsuarioModel.class))).thenReturn(logado);
        when(tokenService.gerar(logado)).thenReturn("token-123");

        LoginResponse resposta = service.entrar(new LoginRequest("083.016.613-05", "fusex123"));

        assertEquals("token-123", resposta.token());
        assertEquals(List.of("MEDICO", "BENEFICIARIO"), resposta.usuario().perfis());
        assertEquals(1, resposta.usuario().medicoId());
        assertEquals("222222222-00", resposta.usuario().precCp());
    }

    @Test
    void senhaErrada_lancaCredenciaisInvalidas() {
        when(usuarioRepository.findByLogin("08301661305")).thenReturn(Optional.of(usuario()));
        when(passwordEncoder.matches("errada", "$2a$hash")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.entrar(new LoginRequest("08301661305", "errada")));

        assertEquals(ErrorCode.CREDENCIAIS_INVALIDAS, ex.getErrorCode());
        verifyNoInteractions(tokenService);
    }

    @Test
    void cpfSemCadastro_lancaOMesmoErro() {
        when(usuarioRepository.findByLogin("00000000000")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.entrar(new LoginRequest("00000000000", "fusex123")));

        assertEquals(ErrorCode.CREDENCIAIS_INVALIDAS, ex.getErrorCode());
    }

    @Test
    void corpoVazio_lancaCredenciaisInvalidasSemConsultarOBanco() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.entrar(new LoginRequest(null, null)));

        assertEquals(ErrorCode.CREDENCIAIS_INVALIDAS, ex.getErrorCode());
        verifyNoInteractions(usuarioRepository);
    }
}