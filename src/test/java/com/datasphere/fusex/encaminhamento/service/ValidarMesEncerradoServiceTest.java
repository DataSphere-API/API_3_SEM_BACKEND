package com.datasphere.fusex.encaminhamento.service;

import com.datasphere.fusex.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidarMesEncerradoServiceTest {

    private final ValidarMesEncerradoService service = new ValidarMesEncerradoService();

    @Test
    void ateODia5DoMesSeguinteAindaNaoEncerrou() {
        assertFalse(service.mesEncerrado("2026-10", LocalDate.of(2026, 11, 5)));
        assertThrows(BusinessException.class,
                () -> service.validarMesEncerrado("2026-10", LocalDate.of(2026, 11, 5)));
    }

    @Test
    void aPartirDoDia6DoMesSeguinteEncerrou() {
        assertTrue(service.mesEncerrado("2026-10", LocalDate.of(2026, 11, 6)));
        assertDoesNotThrow(() -> service.validarMesEncerrado("2026-10", LocalDate.of(2026, 12, 1)));
    }

    @Test
    void faturaSemCompetenciaNaoEncerra() {
        assertFalse(service.mesEncerrado(null, LocalDate.of(2026, 11, 20)));
    }
}
