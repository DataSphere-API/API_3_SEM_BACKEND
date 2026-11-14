package com.datasphere.fusex.auth.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.UsuarioLogado;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenServiceTest {

    private final UsuarioLogado usuario = new UsuarioLogado(6, "99351819019", "Fulano de Tal",
            EnumSet.of(Perfil.BENEFICIARIO), "123456789-00", null, null, null, null, null, null, null);

    @Test
    void tokenGerado_devolveOUsuario() {
        TokenService service = new TokenService(Duration.ofHours(8), Clock.systemUTC());

        String token = service.gerar(usuario);

        assertEquals(usuario, service.buscar(token).orElseThrow());
    }

    @Test
    void cadaLoginGeraUmTokenDiferente() {
        TokenService service = new TokenService(Duration.ofHours(8), Clock.systemUTC());

        assertNotEquals(service.gerar(usuario), service.gerar(usuario));
    }

    @Test
    void tokenDesconhecidoOuNulo_naoAutentica() {
        TokenService service = new TokenService(Duration.ofHours(8), Clock.systemUTC());

        assertTrue(service.buscar("inventado").isEmpty());
        assertTrue(service.buscar(null).isEmpty());
    }

    @Test
    void tokenVencido_naoAutentica() {
        TokenService service = new TokenService(Duration.ZERO, Clock.systemUTC());

        String token = service.gerar(usuario);

        assertTrue(service.buscar(token).isEmpty());
    }
}