package com.datasphere.fusex.divergencia.service;

import com.datasphere.fusex.divergencia.SeveridadeDivergencia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ClassificarSeveridadeDivergenciaService {

    private final BigDecimal limiteBaixa;
    private final BigDecimal limiteMedia;

    public ClassificarSeveridadeDivergenciaService(
            @Value("${fusex.divergencia.limite-baixa:5}") BigDecimal limiteBaixa,
            @Value("${fusex.divergencia.limite-media:15}") BigDecimal limiteMedia) {
        this.limiteBaixa = limiteBaixa;
        this.limiteMedia = limiteMedia;
    }

    public SeveridadeDivergencia classificarSeveridadeDivergencia(BigDecimal divergencia, BigDecimal valorContratado) {
        if (valorContratado.compareTo(BigDecimal.ZERO) <= 0) {
            if (divergencia.compareTo(BigDecimal.ZERO) > 0) {
                return SeveridadeDivergencia.ALTA;
            }
            return SeveridadeDivergencia.BAIXA;
        }

        BigDecimal percentual = divergencia
                .multiply(new BigDecimal("100"))
                .divide(valorContratado, 2, RoundingMode.HALF_UP);

        if (percentual.compareTo(limiteBaixa) <= 0) {
            return SeveridadeDivergencia.BAIXA;
        }

        if (percentual.compareTo(limiteMedia) <= 0) {
            return SeveridadeDivergencia.MEDIA;
        }

        return SeveridadeDivergencia.ALTA;
    }
}