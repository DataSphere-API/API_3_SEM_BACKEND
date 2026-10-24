package com.datasphere.fusex.guia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class ValidadeGuiaService {

    private final long validadeDias;

    public ValidadeGuiaService(@Value("${fusex.guia.validade-dias:60}") long validadeDias) {
        this.validadeDias = validadeDias;
    }

    public long getValidadeDias() {
        return validadeDias;
    }

    public LocalDate calcularDataValidade(LocalDate dataEmissao) {
        return dataEmissao.plusDays(validadeDias);
    }

    public long calcularDiasRestantes(LocalDate dataEmissao) {
        long diasDesdeEmissao = ChronoUnit.DAYS.between(dataEmissao, LocalDate.now());
        return Math.max(0, validadeDias - diasDesdeEmissao);
    }

    public LocalDate calcularLimiteDeEmissaoVencida() {
        return LocalDate.now().minusDays(validadeDias);
    }
}