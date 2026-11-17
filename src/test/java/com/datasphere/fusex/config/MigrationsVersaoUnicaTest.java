package com.datasphere.fusex.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationsVersaoUnicaTest {

    private static final Path PASTA_DB = Path.of("src/main/resources/db");

    @Test
    void cadaVersaoDeMigrationApareceUmaVezSo() throws IOException {
        Map<String, String> arquivoPorVersao = new HashMap<>();
        List<String> repetidas = new ArrayList<>();

        try (Stream<Path> arquivos = Files.walk(PASTA_DB)) {
            for (Path arquivo : arquivos.filter(Files::isRegularFile).toList()) {
                String nome = arquivo.getFileName().toString();
                if (!nome.startsWith("V") || !nome.contains("__")) {
                    continue;
                }
                String versao = nome.substring(1, nome.indexOf("__"));
                String anterior = arquivoPorVersao.put(versao, nome);
                if (anterior != null) {
                    repetidas.add(anterior + " e " + nome);
                }
            }
        }

        assertTrue(repetidas.isEmpty(), "Migrations com a mesma versão: " + repetidas);
    }
}
