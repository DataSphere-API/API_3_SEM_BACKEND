package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.preguia.dto.PacoteSireResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GerarDocumentoConferenciaServiceTest {

    private final GerarDocumentoConferenciaService service = new GerarDocumentoConferenciaService(null);

    private PacoteSireResponse pacote(PacoteSireResponse.AprovadoPor aprovadoPor) {
        return new PacoteSireResponse(
                "1.0", 7L, "APROVADA", LocalDateTime.of(2026, 11, 17, 10, 0), aprovadoPor,
                new PacoteSireResponse.Beneficiario("123456789-00", "Fulano", LocalDate.of(1986, 3, 10), false, "Titular", "UG"),
                new PacoteSireResponse.Medico("12345", "Carlos Medeiros"),
                List.of(new PacoteSireResponse.Procedimento(1L, "TC", "12345678000199", "Clinica X", 1,
                        new BigDecimal("720.49"), new BigDecimal("720.49"))),
                new BigDecimal("720.49"), 60, "a".repeat(64));
    }

    @Test
    void documentoMostraQuemAprovouComDataEHora() {
        PacoteSireResponse.AprovadoPor chefe = new PacoteSireResponse.AprovadoPor(
                1, "Roberto Lima", "CHEFE", LocalDateTime.of(2026, 11, 17, 9, 30, 15));

        List<String> linhas = service.montarLinhas(pacote(chefe));

        assertTrue(linhas.contains("Aprovada por: Roberto Lima (Chefe do FUSEX)"));
        assertTrue(linhas.contains("Usuario: 1"));
        assertTrue(linhas.contains("Data e hora: 17/11/2026 09:30:15"));
    }

    @Test
    void preGuiaSemAssinatura_mostraAvisoNoLugarDaAprovacao() {
        List<String> linhas = service.montarLinhas(pacote(new PacoteSireResponse.AprovadoPor(null, null, null, null)));

        assertTrue(linhas.contains("Sem aprovacao registrada."));
    }
}
