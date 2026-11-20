package com.datasphere.fusex.auth.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidarPrecCpServiceTest {

    private final ValidarPrecCpService service = new ValidarPrecCpService();

    @Test
    void formatoCorreto_naoLancaErro() {
        assertDoesNotThrow(() -> service.validarFormato("123456789-00"));
        assertDoesNotThrow(() -> service.validarFormato("123456789-01"));
    }

    @Test
    void formatoErrado_lancaPrecCpInvalido() {
        for (String invalido : new String[]{null, "", "12345678900", "123456789-0", "12345678-000", "abcdefghi-00"}) {
            BusinessException ex = assertThrows(BusinessException.class, () -> service.validarFormato(invalido));
            assertEquals(ErrorCode.PREC_CP_INVALIDO, ex.getErrorCode());
        }
    }

    @Test
    void titularTerminaEm00() {
        assertTrue(service.ehTitular("123456789-00"));
        assertFalse(service.ehTitular("123456789-01"));
    }

    @Test
    void baseSaoOsNovePrimeirosNumeros() {
        assertEquals("123456789", service.base("123456789-02"));
    }
}