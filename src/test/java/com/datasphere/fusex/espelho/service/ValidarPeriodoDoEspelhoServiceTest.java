package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidarPeriodoDoEspelhoServiceTest {

    private final ValidarPeriodoDoEspelhoService service = new ValidarPeriodoDoEspelhoService();

    private ErrorCode erro(String inicio, String fim, String hoje) {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.validar(LocalDate.parse(inicio), LocalDate.parse(fim), LocalDate.parse(hoje)));
        return ex.getErrorCode();
    }

    @Test
    void mesAtualAteHojeValida() {
        assertDoesNotThrow(() -> service.validar(LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-19"),
                LocalDate.parse("2026-11-19")));
    }

    @Test
    void mesAnteriorAteODia5Valida() {
        assertDoesNotThrow(() -> service.validar(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-31"),
                LocalDate.parse("2026-11-05")));
    }

    @Test
    void mesAnteriorDepoisDoDia5DevolveForaDoMes() {
        assertEquals(ErrorCode.FORA_DO_MES, erro("2026-10-01", "2026-10-31", "2026-11-06"));
    }

    @Test
    void doisMesesDevolveForaDoMes() {
        assertEquals(ErrorCode.FORA_DO_MES, erro("2026-10-25", "2026-11-03", "2026-11-04"));
    }

    @Test
    void fimAntesDoInicioOuDepoisDeHojeDevolveDadosInvalidos() {
        assertEquals(ErrorCode.DADOS_INVALIDOS, erro("2026-11-10", "2026-11-01", "2026-11-19"));
        assertEquals(ErrorCode.DADOS_INVALIDOS, erro("2026-11-01", "2026-11-20", "2026-11-19"));
    }

    @Test
    void viradaDoAnoAceitaDezembroAteODia5DeJaneiro() {
        assertDoesNotThrow(() -> service.validar(LocalDate.parse("2026-12-01"), LocalDate.parse("2026-12-31"),
                LocalDate.parse("2027-01-05")));
    }
}
