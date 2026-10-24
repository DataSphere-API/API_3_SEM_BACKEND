package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.preguia.dto.PacoteSireResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CalcularHashPacoteServiceTest {

    private final CalcularHashPacoteService service = new CalcularHashPacoteService();

    private final PacoteSireResponse.Beneficiario beneficiario = new PacoteSireResponse.Beneficiario(
            "123456789-00", "Fulano", LocalDate.of(1986, 3, 10), false, "Titular", "UG");
    private final PacoteSireResponse.Medico medico = new PacoteSireResponse.Medico("SP123456", "Carlos");
    private final PacoteSireResponse.AprovadoPor aprovadoPor = new PacoteSireResponse.AprovadoPor(
            1, LocalDateTime.of(2026, 10, 12, 9, 0), LocalDateTime.of(2026, 10, 11, 9, 0));

    private List<PacoteSireResponse.Procedimento> procedimentos(int sessoes) {
        return List.of(new PacoteSireResponse.Procedimento(
                1L, "TC", "12345678000199", "Clínica X", sessoes, new BigDecimal("720.49"), new BigDecimal("720.49")));
    }

    @Test
    void mesmosDados_geramOMesmoHash() {
        String primeiro = service.calcularHash(7L, beneficiario, medico, aprovadoPor, procedimentos(1));
        String segundo = service.calcularHash(7L, beneficiario, medico, aprovadoPor, procedimentos(1));

        assertEquals(primeiro, segundo);
        assertEquals(64, primeiro.length());
    }

    @Test
    void dadosAlterados_geramOutroHash() {
        String original = service.calcularHash(7L, beneficiario, medico, aprovadoPor, procedimentos(1));
        String alterado = service.calcularHash(7L, beneficiario, medico, aprovadoPor, procedimentos(2));

        assertFalse(original.equals(alterado));
    }
}