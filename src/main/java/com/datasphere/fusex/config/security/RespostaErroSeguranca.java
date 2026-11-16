package com.datasphere.fusex.config.security;

import com.datasphere.fusex.domain.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RespostaErroSeguranca {

    public void naoAutenticado(HttpServletRequest request, HttpServletResponse response,
                               AuthenticationException ex) throws IOException {
        escrever(response, ErrorCode.NAO_AUTENTICADO);
    }

    public void acessoNegado(HttpServletRequest request, HttpServletResponse response,
                             AccessDeniedException ex) throws IOException {
        escrever(response, ErrorCode.ACESSO_NEGADO);
    }

    private void escrever(HttpServletResponse response, ErrorCode erro) throws IOException {
        response.setStatus(erro.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"codigo\":\"" + erro.getCodigo()
                + "\",\"mensagem\":\"" + erro.getMensagem()
                + "\",\"detalhes\":{}}");
    }
}