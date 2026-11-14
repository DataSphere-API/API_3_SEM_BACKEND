package com.datasphere.fusex.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QrConsultaMockFilterTest {

    @Test
    void mockDeIdentidadeSoERegistradoComDoisPerfisLocais() {
        for (String[] perfis : new String[][]{{}, {"dev"}, {"qrconsulta-mock"}, {"dev", "qrconsulta-mock"}}) {
            try (var contexto = new AnnotationConfigApplicationContext()) {
                contexto.getEnvironment().setActiveProfiles(perfis);
                contexto.register(QrConsultaMockFilter.class);
                contexto.refresh();
                assertEquals(perfis.length == 2, !contexto.getBeansOfType(QrConsultaMockFilter.class).isEmpty());
            }
        }
    }
}
