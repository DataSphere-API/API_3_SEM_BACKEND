package com.datasphere.fusex.guia.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidadeGuiaServiceTest {

    private final ValidadeGuiaService service = new ValidadeGuiaService(60);

    @Test
    void dataDeValidade_eEmissaoMais60Dias() {
        assertEquals(LocalDate.of(2026, 11, 30), service.calcularDataValidade(LocalDate.of(2026, 10, 1)));
    }

    @Test
    void diasRestantes_descontaOsDiasDesdeAEmissao() {
        assertEquals(52L, service.calcularDiasRestantes(LocalDate.now().minusDays(8)));
    }

    @Test
    void guiaVencida_naoTemDiasRestantesNegativos() {
        assertEquals(0L, service.calcularDiasRestantes(LocalDate.now().minusDays(100)));
    }

    @Test
    void limiteDeEmissaoVencida_eHojeMenos60Dias() {
        assertEquals(LocalDate.now().minusDays(60), service.calcularLimiteDeEmissaoVencida());
    }
}