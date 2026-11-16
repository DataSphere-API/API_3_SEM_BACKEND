package com.datasphere.fusex.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Principal;

/** Identidade simulada, exclusivamente nos testes locais da US003-5. */
@Component
@Profile("dev & qrconsulta-mock")
public class QrConsultaMockFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String login = request.getHeader("X-Mock-Login");
        if (request.getUserPrincipal() != null || login == null || login.isBlank()) {
            chain.doFilter(request, response);
            return;
        }
        chain.doFilter(new HttpServletRequestWrapper(request) {
            @Override
            public Principal getUserPrincipal() {
                return () -> login.trim();
            }
        }, response);
    }
}
