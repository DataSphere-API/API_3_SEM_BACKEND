package com.datasphere.fusex.auth.service;

import com.datasphere.fusex.auth.UsuarioLogado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private final Map<String, SessaoAtiva> sessoes = new ConcurrentHashMap<>();
    private final SecureRandom aleatorio = new SecureRandom();
    private final Duration validade;
    private final Clock relogio;

    @Autowired
    public TokenService(@Value("${fusex.auth.token-validade-horas:8}") long horas) {
        this(Duration.ofHours(horas), Clock.systemDefaultZone());
    }

    // Usado nos testes para controlar a validade.
    TokenService(Duration validade, Clock relogio) {
        this.validade = validade;
        this.relogio = relogio;
    }

    public String gerar(UsuarioLogado usuario) {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessoes.put(token, new SessaoAtiva(usuario, Instant.now(relogio).plus(validade)));
        return token;
    }

    public Optional<UsuarioLogado> buscar(String token) {
        if (token == null) {
            return Optional.empty();
        }
        SessaoAtiva sessao = sessoes.get(token);
        if (sessao == null) {
            return Optional.empty();
        }
        if (!Instant.now(relogio).isBefore(sessao.expiraEm())) {
            sessoes.remove(token);
            return Optional.empty();
        }
        return Optional.of(sessao.usuario());
    }

    private record SessaoAtiva(UsuarioLogado usuario, Instant expiraEm) {}
}