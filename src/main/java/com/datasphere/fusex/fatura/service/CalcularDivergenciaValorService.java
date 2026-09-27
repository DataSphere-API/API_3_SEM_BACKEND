package com.datasphere.fusex.fatura.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CalcularDivergenciaValorService {

    public BigDecimal calcularDivergenciaValor(BigDecimal valorApresentado, BigDecimal valorContratado) {
        return valorApresentado.subtract(valorContratado);
    }
}