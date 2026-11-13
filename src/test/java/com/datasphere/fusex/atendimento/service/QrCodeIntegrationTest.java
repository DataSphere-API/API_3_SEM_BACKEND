package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.atendimento.controller.QrCodeController;
import com.datasphere.fusex.exception.GlobalExceptionHandler;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.docker.compose.enabled=false",
        "spring.datasource.url=${US0031_TEST_DB_URL}",
        "spring.datasource.username=fusex_test",
        "spring.datasource.password=fusex_test_local",
        "spring.jpa.hibernate.ddl-auto=none",
        "logging.level.root=WARN",
        "debug=false"
})
@ActiveProfiles("dev")
@EnabledIfEnvironmentVariable(named = "US0031_TEST_DB_URL", matches = ".+")
@Transactional
class QrCodeIntegrationTest {

    @Autowired private QrCodeController controller;
    @Autowired private GarantirQrDoProcedimentoService garantirQrDoProcedimentoService;
    @Autowired private GlobalExceptionHandler exceptionHandler;
    @Autowired private JdbcTemplate jdbc;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(exceptionHandler).build();
    }

    private Long criarGuia(String status, LocalDate emissao) {
        return jdbc.queryForObject("""
                INSERT INTO guia (benf_id, med_id, emissor_id, chefe_id, status, data_emissao)
                VALUES ('123456789-00', 1, 1, 1, ?, ?) RETURNING id
                """, Long.class, status, emissao);
    }

    private Long criarProcedimento(Long guiaId, String status, String qr) {
        return jdbc.queryForObject("""
                INSERT INTO guia_proc (cont_id, guia_id, status, qr, qtd_autorizada, qtd_realizada)
                VALUES (1, ?, ?, ?, 1, 0) RETURNING id
                """, Long.class, guiaId, status, qr);
    }

    @Test
    void geraUuidGravaNoProcedimentoEASegundaChamadaReaproveita() throws Exception {
        Long guiaId = criarGuia("EMITIDA", LocalDate.now());
        Long id = criarProcedimento(guiaId, "PENDENTE", null);
        String resposta = mvc.perform(get("/atendimentos/{id}/qrcode", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atendimentoId").value(id))
                .andExpect(jsonPath("$.imagemBase64").value(org.hamcrest.Matchers.startsWith("data:image/png;base64,")))
                .andReturn().getResponse().getContentAsString();
        String codigo = JsonPath.read(resposta, "$.codigo");
        assertNotNull(UUID.fromString(codigo));
        assertEquals(codigo, jdbc.queryForObject("SELECT qr FROM guia_proc WHERE id=?", String.class, id));
        mvc.perform(get("/atendimentos/{id}/qrcode", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.imagemBase64").value((String) JsonPath.read(resposta, "$.imagemBase64")));
    }

    @Test
    void procedimentosDaMesmaGuiaRecebemQrDiferentes() {
        Long guiaId = criarGuia("PARCIALMENTE_REALIZADA", LocalDate.now());
        Long primeiro = criarProcedimento(guiaId, "PENDENTE", null);
        Long segundo = criarProcedimento(guiaId, "EM_ANDAMENTO", null);
        assertNotEquals(garantirQrDoProcedimentoService.garantirQrDoProcedimento(primeiro),
                garantirQrDoProcedimentoService.garantirQrDoProcedimento(segundo));
    }

    @Test
    void qrJaPersistidoEReutilizadoSemExigirFormatoUuid() throws Exception {
        Long guiaId = criarGuia("EMITIDA", LocalDate.now());
        Long id = criarProcedimento(guiaId, "PENDENTE", "QR-LEGADO-US003-1");
        mvc.perform(get("/atendimentos/{id}/qrcode", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("QR-LEGADO-US003-1"));
    }

    @Test
    void guiaMaisAntigaQueSessentaDiasNaoGeraQrMesmoSemValidadeNoProcedimento() throws Exception {
        Long guiaId = criarGuia("EMITIDA", LocalDate.now().minusDays(61));
        Long id = criarProcedimento(guiaId, "PENDENTE", null);
        mvc.perform(get("/atendimentos/{id}/qrcode", id)).andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("GUIA_EXPIRADA"));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM guia_proc WHERE id=? AND qr IS NOT NULL", Integer.class, id));
    }

    @Test
    void procedimentoRealizadoNaoReaproveitaQr() throws Exception {
        Long guiaId = criarGuia("EMITIDA", LocalDate.now());
        Long id = criarProcedimento(guiaId, "REALIZADA", "QR-REALIZADO-US003-1");
        mvc.perform(get("/atendimentos/{id}/qrcode", id)).andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("PROCEDIMENTO_JA_REALIZADO"));
    }

    @Test
    void atendimentoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/atendimentos/999999999/qrcode")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ATENDIMENTO_NAO_ENCONTRADO"));
    }

    @Test
    void bancoImpedeMesmoCodigoEmDoisProcedimentos() {
        Long guiaId = criarGuia("EMITIDA", LocalDate.now());
        criarProcedimento(guiaId, "PENDENTE", "QR-DUPLICADO-US003-1");
        assertThrows(DataIntegrityViolationException.class,
                () -> criarProcedimento(guiaId, "PENDENTE", "QR-DUPLICADO-US003-1"));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void chamadasSimultaneasConfirmamOMesmoCodigoNoBanco() throws Exception {
        Long guiaId = criarGuia("EMITIDA", LocalDate.now());
        Long id = criarProcedimento(guiaId, "PENDENTE", null);
        CountDownLatch prontos = new CountDownLatch(2);
        CountDownLatch iniciar = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<String> gerar = () -> {
                prontos.countDown();
                assertTrue(iniciar.await(10, TimeUnit.SECONDS));
                return garantirQrDoProcedimentoService.garantirQrDoProcedimento(id);
            };
            var primeiro = executor.submit(gerar);
            var segundo = executor.submit(gerar);
            assertTrue(prontos.await(10, TimeUnit.SECONDS));
            iniciar.countDown();
            String codigo = primeiro.get(15, TimeUnit.SECONDS);
            assertEquals(codigo, segundo.get(15, TimeUnit.SECONDS));
            assertNotNull(UUID.fromString(codigo));
            assertEquals(codigo, jdbc.queryForObject("SELECT qr FROM guia_proc WHERE id=?", String.class, id));
        } finally {
            iniciar.countDown();
            jdbc.update("DELETE FROM guia_proc WHERE id=?", id);
            jdbc.update("DELETE FROM guia WHERE id=?", guiaId);
        }
    }
}
