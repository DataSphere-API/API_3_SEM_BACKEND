package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
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

    @Autowired private GarantirQrDoProcedimentoService garantirQrDoProcedimentoService;
    @Autowired private JdbcTemplate jdbc;

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
        String codigo = garantirQrDoProcedimentoService.garantirQrDoProcedimento(id);
        assertNotNull(UUID.fromString(codigo));
        assertEquals(codigo, jdbc.queryForObject("SELECT qr FROM guia_proc WHERE id=?", String.class, id));
        assertEquals(codigo, garantirQrDoProcedimentoService.garantirQrDoProcedimento(id));
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
        assertEquals("QR-LEGADO-US003-1", garantirQrDoProcedimentoService.garantirQrDoProcedimento(id));
    }

    @Test
    void guiaMaisAntigaQueSessentaDiasNaoGeraQrMesmoSemValidadeNoProcedimento() throws Exception {
        Long guiaId = criarGuia("EMITIDA", LocalDate.now().minusDays(61));
        Long id = criarProcedimento(guiaId, "PENDENTE", null);
        var erro = assertThrows(BusinessException.class,
                () -> garantirQrDoProcedimentoService.garantirQrDoProcedimento(id));
        assertEquals(ErrorCode.GUIA_EXPIRADA, erro.getErrorCode());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM guia_proc WHERE id=? AND qr IS NOT NULL", Integer.class, id));
    }

    @Test
    void procedimentoRealizadoNaoReaproveitaQr() throws Exception {
        Long guiaId = criarGuia("EMITIDA", LocalDate.now());
        Long id = criarProcedimento(guiaId, "REALIZADA", "QR-REALIZADO-US003-1");
        var erro = assertThrows(BusinessException.class,
                () -> garantirQrDoProcedimentoService.garantirQrDoProcedimento(id));
        assertEquals(ErrorCode.PROCEDIMENTO_JA_REALIZADO, erro.getErrorCode());
    }

    @Test
    void atendimentoInexistenteRetorna404() throws Exception {
        var erro = assertThrows(BusinessException.class,
                () -> garantirQrDoProcedimentoService.garantirQrDoProcedimento(999999999L));
        assertEquals(ErrorCode.ATENDIMENTO_NAO_ENCONTRADO, erro.getErrorCode());
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
