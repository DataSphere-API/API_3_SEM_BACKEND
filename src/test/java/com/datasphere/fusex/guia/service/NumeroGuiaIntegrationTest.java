package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.config.QrConsultaMockFilter;
import com.datasphere.fusex.exception.GlobalExceptionHandler;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guia.controller.BeneficiarioGuiaController;
import com.datasphere.fusex.preguia.service.EmitirGuiaService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.docker.compose.enabled=false",
        "spring.datasource.url=${US0036_TEST_DB_URL}",
        "spring.datasource.username=fusex_test",
        "spring.datasource.password=fusex_test_local",
        "spring.jpa.hibernate.ddl-auto=none",
        "logging.level.root=WARN",
        "debug=false"
})
@ActiveProfiles({"dev", "qrconsulta-mock", "guias-mock"})
@EnabledIfEnvironmentVariable(named = "US0036_TEST_DB_URL", matches = ".+")
@Transactional
class NumeroGuiaIntegrationTest {

    @Autowired private BeneficiarioGuiaController controller;
    @Autowired private GlobalExceptionHandler exceptionHandler;
    @Autowired private QrConsultaMockFilter mockFilter;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private GuiaRepository guiaRepository;
    @Autowired private EmitirGuiaService emitirGuiaService;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(exceptionHandler)
                .addFilters(mockFilter).build();
    }

    private Long inserirGuia(JdbcTemplate banco, LocalDate emissao) {
        return banco.queryForObject("""
                INSERT INTO guia (benf_id, med_id, emissor_id, chefe_id, status, data_emissao)
                VALUES ('123456789-00', 1, 1, 1, 'EMITIDA', ?) RETURNING id
                """, Long.class, emissao);
    }

    @Test
    void donoRecebeTodasAsGuiasComNumeroInclusiveAntigas() throws Exception {
        mvc.perform(get("/beneficiarios/123456789-00/guias").header("X-Mock-Login", "00000000351"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value(org.hamcrest.Matchers.matchesPattern("[0-9]{4}-[0-9]{6,}")))
                .andExpect(jsonPath("$[0].guiaId").isNumber())
                .andExpect(jsonPath("$[0].procedimentos").isArray());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM guia WHERE numero IS NULL OR numero=''", Integer.class));
        assertEquals(jdbc.queryForObject("SELECT COUNT(*) FROM guia", Integer.class),
                jdbc.queryForObject("SELECT COUNT(DISTINCT numero) FROM guia", Integer.class));
    }

    @Test
    void outroBeneficiarioRecebe403MesmoForjandoHeaderDeDono() throws Exception {
        mvc.perform(get("/beneficiarios/123456789-00/guias").header("X-Mock-Login", "00000000352")
                        .header("X-Prec-Cp", "123456789-00"))
                .andExpect(status().isForbidden());
    }

    @Test
    void chefeEEmissorPodemConsultarOutrosBeneficiarios() throws Exception {
        for (String login : new String[]{"00000000353", "00000000354"}) {
            mvc.perform(get("/beneficiarios/123456789-00/guias").header("X-Mock-Login", login))
                    .andExpect(status().isOk()).andExpect(jsonPath("$[0].numero").isString());
        }
    }

    @Test
    void medicoNaoGanhaPermissaoDeChefeOuEmissor() throws Exception {
        mvc.perform(get("/beneficiarios/123456789-00/guias").header("X-Mock-Login", "00000000355"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/beneficiarios/222222222-00/guias").header("X-Mock-Login", "00000000355"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void semAutenticacaoRecebe401EHeaderPrecCpNaoAutentica() throws Exception {
        mvc.perform(get("/beneficiarios/123456789-00/guias")).andExpect(status().isUnauthorized());
        mvc.perform(get("/beneficiarios/123456789-00/guias").header("X-Prec-Cp", "123456789-00"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void terceiroNaoDescobreBeneficiarioAusenteEChefeRecebe404() throws Exception {
        mvc.perform(get("/beneficiarios/inexistente/guias").header("X-Mock-Login", "00000000351"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/beneficiarios/inexistente/guias").header("X-Mock-Login", "00000000353"))
                .andExpect(status().isNotFound());
    }

    @Test
    void sequenciaRecomecaEmCadaAno() {
        Long primeiro = inserirGuia(jdbc, LocalDate.of(2037, 1, 1));
        Long segundo = inserirGuia(jdbc, LocalDate.of(2037, 12, 31));
        Long outroAno = inserirGuia(jdbc, LocalDate.of(2038, 1, 1));
        assertEquals("2037-000001", jdbc.queryForObject("SELECT numero FROM guia WHERE id=?", String.class, primeiro));
        assertEquals("2037-000002", jdbc.queryForObject("SELECT numero FROM guia WHERE id=?", String.class, segundo));
        assertEquals("2038-000001", jdbc.queryForObject("SELECT numero FROM guia WHERE id=?", String.class, outroAno));
    }

    @Test
    void naoTruncaNumeroQuandoPassaDeSeisDigitos() {
        jdbc.update("INSERT INTO guia_numeracao_anual (ano, ultimo_numero) VALUES (2039, 999999)");
        Long id = inserirGuia(jdbc, LocalDate.of(2039, 1, 1));
        assertEquals("2039-1000000", jdbc.queryForObject("SELECT numero FROM guia WHERE id=?", String.class, id));
    }

    @Test
    void jpaRecebeNumeroGeradoPeloBancoNaEmissao() {
        var guia = new GuiaModel();
        guia.setBenfId("123456789-00");
        guia.setStatus("EMITIDA");
        guia.setDataEmissao(LocalDate.now());
        GuiaModel salva = guiaRepository.saveAndFlush(guia);
        assertNotNull(salva.getNumero());
        assertEquals(salva.getNumero(), jdbc.queryForObject("SELECT numero FROM guia WHERE id=?", String.class, salva.getId()));
    }

    @Test
    void emissaoRealDaPreGuiaPersisteNumeroEConservaQrPorProcedimento() {
        Long preGuiaId = jdbc.queryForObject("""
                INSERT INTO pre_guia (status, benf_id, med_id, emissor_id, chefe_id)
                VALUES ('APROVADA', '123456789-00', 1, 1, 1) RETURNING id
                """, Long.class);
        jdbc.update("""
                INSERT INTO pre_guia_proc (pre_guia_id, proc_id, ocs_id, qtd_autorizada)
                VALUES (?, 1, '12345678000199', 1)
                """, preGuiaId);
        var emissao = emitirGuiaService.emitir(preGuiaId);
        String numero = jdbc.queryForObject("SELECT numero FROM guia WHERE id=?", String.class, emissao.guiaId());
        assertNotNull(numero);
        assertTrue(numero.matches(LocalDate.now().getYear() + "-[0-9]{6,}"));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM guia_proc WHERE guia_id=? AND qr IS NOT NULL",
                Integer.class, emissao.guiaId()));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void emissoesConcorrentesNaoDuplicamNumeros() throws Exception {
        List<Long> ids = new ArrayList<>();
        CountDownLatch iniciar = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(4)) {
            Callable<Long> emitir = () -> {
                assertTrue(iniciar.await(10, TimeUnit.SECONDS));
                return inserirGuia(jdbc, LocalDate.of(2041, 1, 1));
            };
            var futuros = List.of(executor.submit(emitir), executor.submit(emitir), executor.submit(emitir), executor.submit(emitir));
            iniciar.countDown();
            for (var futuro : futuros) ids.add(futuro.get(15, TimeUnit.SECONDS));
            var numeros = ids.stream().map(id -> jdbc.queryForObject("SELECT numero FROM guia WHERE id=?", String.class, id)).sorted().toList();
            assertEquals(List.of("2041-000001", "2041-000002", "2041-000003", "2041-000004"), numeros);
        } finally {
            iniciar.countDown();
            for (Long id : ids) jdbc.update("DELETE FROM guia WHERE id=?", id);
            jdbc.update("DELETE FROM guia_numeracao_anual WHERE ano=2041");
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void migrationPreencheHistoricoPorAnoEContinuaSequencia() {
        String schema = "us0036_backfill_test";
        jdbc.execute("CREATE SCHEMA " + schema);
        String url = System.getenv("US0036_TEST_DB_URL");
        var ds = new DriverManagerDataSource(url + (url.contains("?") ? "&" : "?") + "currentSchema=" + schema,
                "fusex_test", "fusex_test_local");
        var banco = new JdbcTemplate(ds);
        try {
            Flyway.configure().dataSource(ds).schemas(schema).defaultSchema(schema)
                    .locations("classpath:db/migration", "classpath:db/dev", "classpath:db/qrconsulta-mock")
                    .target("17").load().migrate();
            Long segundo = inserirGuia(banco, LocalDate.of(1997, 6, 1));
            Long primeiro = inserirGuia(banco, LocalDate.of(1997, 1, 1));
            Flyway.configure().dataSource(ds).schemas(schema).defaultSchema(schema)
                    .locations("classpath:db/migration", "classpath:db/dev", "classpath:db/qrconsulta-mock")
                    .target("18").load().migrate();
            assertEquals("1997-000001", banco.queryForObject("SELECT numero FROM guia WHERE id=?", String.class, primeiro));
            assertEquals("1997-000002", banco.queryForObject("SELECT numero FROM guia WHERE id=?", String.class, segundo));
            Long terceiro = inserirGuia(banco, LocalDate.of(1997, 12, 31));
            assertEquals("1997-000003", banco.queryForObject("SELECT numero FROM guia WHERE id=?", String.class, terceiro));
            assertEquals(0, banco.queryForObject("SELECT COUNT(*) FROM guia WHERE numero IS NULL", Integer.class));
        } finally {
            jdbc.execute("DROP SCHEMA " + schema + " CASCADE");
        }
    }
}
