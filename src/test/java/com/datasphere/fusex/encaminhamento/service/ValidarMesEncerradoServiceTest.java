package com.datasphere.fusex.encaminhamento.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidarMesEncerradoServiceTest {

    private final ValidarMesEncerradoService service = new ValidarMesEncerradoService();

    @Test
    void rejeitaEncaminhamentoNoDiaCincoComErro422() {
        var erro = assertThrows(BusinessException.class, () -> service.validarMesEncerrado(
                YearMonth.of(2026, 10), LocalDate.of(2026, 11, 5)));

        assertEquals(ErrorCode.MES_NAO_ENCERRADO, erro.getErrorCode());
        assertEquals(422, erro.getErrorCode().getStatus().value());
    }

    @Test
    void liberaEncaminhamentoNoDiaSeis() {
        assertDoesNotThrow(() -> service.validarMesEncerrado(
                YearMonth.of(2026, 10), LocalDate.of(2026, 11, 6)));
    }

    @Test
    void liberaEncaminhamentoDaCompetenciaDeDezembroEmJaneiro() {
        assertDoesNotThrow(() -> service.validarMesEncerrado(
                YearMonth.of(2026, 12), LocalDate.of(2027, 1, 7)));
    }
}
