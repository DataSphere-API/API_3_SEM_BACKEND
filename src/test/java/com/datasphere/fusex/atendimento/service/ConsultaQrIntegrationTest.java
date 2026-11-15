package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.atendimento.controller.QrCodeController;
import com.datasphere.fusex.config.QrConsultaMockFilter;
import com.datasphere.fusex.dto.request.ValidarQrCodeRequest;
import com.datasphere.fusex.exception.GlobalExceptionHandler;
import com.datasphere.fusex.preguia.service.EmitirGuiaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.docker.compose.enabled=false",
        "spring.datasource.url=${US0035_TEST_DB_URL}",
        "spring.datasource.username=fusex_test",
        "spring.datasource.password=fusex_test_local",
        "spring.jpa.hibernate.ddl-auto=none",
        "logging.level.root=WARN",
        "debug=false"
})
@ActiveProfiles({"dev", "qrconsulta-mock"})
@EnabledIfEnvironmentVariable(named = "US0035_TEST_DB_URL", matches = ".+")
@Transactional
class ConsultaQrIntegrationTest {

    @Autowired private QrCodeController controller;
    @Autowired private GlobalExceptionHandler exceptionHandler;
    @Autowired private QrConsultaMockFilter mockFilter;
    @Autowired private EmitirGuiaService emitirGuiaService;
    @Autowired private RegistrarRealizacaoService registrarRealizacaoService;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private DataSource dataSource;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(exceptionHandler)
                .addFilters(mockFilter).build();
    }

    @Test
    void donoRecebeSomenteOCodigoPersistidoESegundaChamadaReaproveita() throws Exception {
        String codigo = jdbc.queryForObject("SELECT qr FROM guia_proc WHERE id=4", String.class);
        for (int i = 0; i < 2; i++) {
            mvc.perform(get("/atendimentos/4/qrcode").header("X-Mock-Login", "00000000351"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$.codigo").value(codigo));
        }
        assertEquals(codigo, jdbc.queryForObject("SELECT qr FROM guia_proc WHERE id=4", String.class));
    }

    @Test
    void outroBeneficiarioRecebe403MesmoInformandoPrecCpDoDono() throws Exception {
        mvc.perform(get("/atendimentos/4/qrcode").header("X-Mock-Login", "00000000352")
                        .header("X-Prec-Cp", "123456789-00"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("BENEFICIARIO_SEM_PERMISSAO"));
    }

    @Test
    void semIdentidadeOuComLoginSemBeneficiarioNaoRecebeQr() throws Exception {
        mvc.perform(get("/atendimentos/4/qrcode")).andExpect(status().isUnauthorized());
        mvc.perform(get("/atendimentos/4/qrcode").header("X-Prec-Cp", "123456789-00"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/atendimentos/4/qrcode").header("X-Mock-Login", "inexistente"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sessoesIncompletasRecebem409SemExporCodigo() throws Exception {
        mvc.perform(get("/atendimentos/5/qrcode").header("X-Mock-Login", "00000000351"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("SESSOES_INCOMPLETAS"));
    }

    @Test
    void procedimentoRealizadoRecebe409() throws Exception {
        mvc.perform(get("/atendimentos/6/qrcode").header("X-Mock-Login", "00000000351"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("PROCEDIMENTO_JA_REALIZADO"));
    }

    @Test
    void guiaExpiradaRecebe409ParaDonoE403ParaOutro() throws Exception {
        mvc.perform(get("/atendimentos/3/qrcode").header("X-Mock-Login", "00000000352"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("GUIA_EXPIRADA"));
        mvc.perform(get("/atendimentos/3/qrcode").header("X-Mock-Login", "00000000351"))
                .andExpect(status().isForbidden());
    }

    @Test
    void consultaNaoCriaCodigoAusente() throws Exception {
        jdbc.update("UPDATE guia_proc SET qr=NULL WHERE id=4");
        mvc.perform(get("/atendimentos/4/qrcode").header("X-Mock-Login", "00000000351"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("QR_NAO_GERADO"));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM guia_proc WHERE id=4 AND qr IS NOT NULL", Integer.class));
    }

    @Test
    void atendimentoAusenteMantem404() throws Exception {
        mvc.perform(get("/atendimentos/999999999/qrcode").header("X-Mock-Login", "00000000351"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("ATENDIMENTO_NAO_ENCONTRADO"));
    }

    @Test
    void emissaoDeTresProcedimentosJaPersisteTresUuidDiferentes() {
        Long preGuiaId = jdbc.queryForObject("""
                INSERT INTO pre_guia (status, benf_id, med_id, emissor_id, chefe_id)
                VALUES ('APROVADA', '123456789-00', 1, 1, 1) RETURNING id
                """, Long.class);
        jdbc.update("""
                INSERT INTO pre_guia_proc (pre_guia_id, proc_id, ocs_id, qtd_autorizada)
                VALUES (?, 1, '12345678000199', 1), (?, 2, '98765432000111', 2), (?, 4, '11222333000144', 1)
                """, preGuiaId, preGuiaId, preGuiaId);
        var emissao = emitirGuiaService.emitir(preGuiaId);
        assertEquals(3, emissao.procedimentos().size());
        var codigos = jdbc.queryForList("SELECT qr FROM guia_proc WHERE guia_id=?", String.class, emissao.guiaId());
        assertEquals(3, codigos.stream().distinct().count());
        for (String codigo : codigos) assertNotNull(UUID.fromString(codigo));
    }

    @Test
    void backfillCompletaAusentesPreservaExistentesEEIdempotente() {
        String preservado = jdbc.queryForObject("SELECT qr FROM guia_proc WHERE id=6", String.class);
        jdbc.update("UPDATE guia_proc SET qr=NULL WHERE id=4");
        jdbc.update("UPDATE guia_proc SET qr='  ' WHERE id=5");
        var script = new ResourceDatabasePopulator(new ClassPathResource("db/migration/V16__preencher_qr_guias_emitidas.sql"));
        script.execute(dataSource);
        String primeiro = jdbc.queryForObject("SELECT qr FROM guia_proc WHERE id=4", String.class);
        String segundo = jdbc.queryForObject("SELECT qr FROM guia_proc WHERE id=5", String.class);
        assertNotNull(UUID.fromString(primeiro));
        assertNotNull(UUID.fromString(segundo));
        assertEquals(preservado, jdbc.queryForObject("SELECT qr FROM guia_proc WHERE id=6", String.class));
        script.execute(dataSource);
        assertEquals(primeiro, jdbc.queryForObject("SELECT qr FROM guia_proc WHERE id=4", String.class));
        assertEquals(segundo, jdbc.queryForObject("SELECT qr FROM guia_proc WHERE id=5", String.class));
    }

    @Test
    void aposBaixaFinalOCodigoNaoPodeSerReutilizado() throws Exception {
        String codigo = jdbc.queryForObject("SELECT qr FROM guia_proc WHERE id=4", String.class);
        registrarRealizacaoService.registrarRealizacao(new ValidarQrCodeRequest(codigo, "12345678000199"));
        mvc.perform(get("/atendimentos/4/qrcode").header("X-Mock-Login", "00000000351"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("PROCEDIMENTO_JA_REALIZADO"));
    }
}
