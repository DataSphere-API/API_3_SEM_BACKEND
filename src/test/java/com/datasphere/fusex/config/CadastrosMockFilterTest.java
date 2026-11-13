package com.datasphere.fusex.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CadastrosMockFilterTest {

    @Test
    void identificacaoMockSoExisteComOsDoisPerfisAtivos() {
        for (String[] perfis : new String[][]{{}, {"dev"}, {"cadastros-mock"}, {"dev", "cadastros-mock"}}) {
            try (var contexto = new AnnotationConfigApplicationContext()) {
                contexto.getEnvironment().setActiveProfiles(perfis);
                contexto.register(CadastrosMockFilter.class);
                contexto.refresh();
                assertEquals(perfis.length == 2, !contexto.getBeansOfType(CadastrosMockFilter.class).isEmpty());
            }
        }
    }
}
