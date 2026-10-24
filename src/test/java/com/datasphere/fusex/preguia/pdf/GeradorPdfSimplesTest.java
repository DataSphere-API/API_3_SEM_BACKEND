package com.datasphere.fusex.preguia.pdf;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeradorPdfSimplesTest {

    @Test
    void geraUmPdfValidoComUmaPagina() {
        byte[] pdf = GeradorPdfSimples.gerar(List.of("Linha 1", "Linha 2 (com parênteses)"));
        String texto = new String(pdf, StandardCharsets.ISO_8859_1);

        assertTrue(texto.startsWith("%PDF-1.4"));
        assertTrue(texto.contains("/Count 1"));
        assertTrue(texto.contains("Linha 2 \\(com parênteses\\)"));
        assertTrue(texto.endsWith("%%EOF\n"));
    }

    @Test
    void textoLongoViraVariasPaginas() {
        List<String> linhas = new ArrayList<>();
        for (int i = 0; i < 120; i++) {
            linhas.add("Linha " + i);
        }

        String texto = new String(GeradorPdfSimples.gerar(linhas), StandardCharsets.ISO_8859_1);

        assertTrue(texto.contains("/Count 3"));
        assertEquals(-1, texto.indexOf("/Count 4"));
    }
}