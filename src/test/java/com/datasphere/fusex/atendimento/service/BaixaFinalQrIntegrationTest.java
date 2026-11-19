package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.auth.dto.LoginRequest;
import com.datasphere.fusex.auth.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.docker.compose.enabled=false",
        "spring.datasource.url=${US0048_TEST_DB_URL}",
        "spring.datasource.username=fusex_test",
        "spring.datasource.password=fusex_test_local",
        "spring.jpa.hibernate.ddl-auto=none",
        "logging.level.root=WARN",
        "debug=false"
})
@ActiveProfiles("dev")
@EnabledIfEnvironmentVariable(named = "US0048_TEST_DB_URL", matches = ".+")
class BaixaFinalQrIntegrationTest {

    @Autowired private WebApplicationContext contexto;
    @Autowired private FilterChainProxy seguranca;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private LoginService loginService;
    private MockMvc mvc;
    private String token;
    private String tokenOutraOcs;
    private Long id;
    private Long guiaId;
    private String qr;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).addFilters(seguranca).build();
        jdbc.update("""
                INSERT INTO usuario (login, senha)
                SELECT '00000000482', senha FROM usuario WHERE login='62819482112'
                ON CONFLICT (login) DO NOTHING
                """);
        jdbc.update("""
                INSERT INTO funocs (cargo, ocs_id, usuario_id)
                SELECT 'Recepção', '98765432000111', id FROM usuario u
                WHERE login='00000000482' AND NOT EXISTS (SELECT 1 FROM funocs f WHERE f.usuario_id=u.id)
                """);
        token = loginService.entrar(new LoginRequest("62819482112", "fusex123")).token();
        tokenOutraOcs = loginService.entrar(new LoginRequest("00000000482", "fusex123")).token();
        guiaId = jdbc.queryForObject("""
                INSERT INTO guia (benf_id, status, data_emissao)
                VALUES ('123456789-00', 'EMITIDA', CURRENT_DATE) RETURNING id
                """, Long.class);
        qr = UUID.randomUUID().toString();
        id = jdbc.queryForObject("""
                INSERT INTO guia_proc (cont_id, guia_id, qr, status, qtd_autorizada, qtd_realizada, data_validade)
                VALUES (1, ?, ?, 'PENDENTE', 2, 0, CURRENT_DATE + 60) RETURNING id
                """, Long.class, guiaId, qr);
    }

    @Test
    void duasSessoesSimultaneasContamDuasEGravamUmHistorico() throws Exception {
        var largada = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var tarefas = java.util.stream.IntStream.range(0, 2).mapToObj(i -> executor.submit(() -> {
                assertTrue(largada.await(10, TimeUnit.SECONDS));
                return registrarSessao().getResponse().getStatus();
            })).toList();
            largada.countDown();
            for (var tarefa : tarefas) assertEquals(201, tarefa.get(20, TimeUnit.SECONDS));
        }
        assertEquals(2, qtdRealizada());
        assertEquals(List.of(1, 2), jdbc.queryForList(
                "SELECT numero_sessao FROM sessao_procedimento WHERE guia_proc_id=? ORDER BY numero_sessao", Integer.class, id));
        assertEquals(1, jdbc.queryForObject("""
                SELECT COUNT(*) FROM historico_status WHERE entidade_tipo='GUIA_PROC'
                AND entidade_id=? AND status_anterior='PENDENTE' AND status_novo='EM_ANDAMENTO'
                """, Integer.class, id));
        baixar(token).andExpect(status().isOk()).andExpect(jsonPath("$.statusProcedimento").value("REALIZADA"));
        baixar(token).andExpect(status().isConflict());
        assertEquals(2, qtdRealizada());
    }

    @Test
    void baixaUnicaCriaSessaoNoBancoESegundaChamadaNaoDuplica() throws Exception {
        jdbc.update("UPDATE guia_proc SET qtd_autorizada=1 WHERE id=?", id);
        mvc.perform(get("/atendimentos/qrcode/" + qr).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.prontoParaBaixa").value(true));
        baixar(token).andExpect(status().isOk()).andExpect(jsonPath("$.qtdRealizada").value(1));
        assertEquals("12345678000199", jdbc.queryForObject(
                "SELECT ocs_id FROM sessao_procedimento WHERE guia_proc_id=?", String.class, id));
        baixar(token).andExpect(status().isConflict());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM sessao_procedimento WHERE guia_proc_id=?", Integer.class, id));
    }

    @Test
    void outraOcsRecebe403MesmoForjandoCnpj() throws Exception {
        mvc.perform(get("/atendimentos/qrcode/" + qr).param("cnpj", "12345678000199")
                        .header("Authorization", "Bearer " + tokenOutraOcs))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("OCS_DIFERENTE_DO_CONTRATO"));
        mvc.perform(post("/atendimentos/validar-qrcode").contentType("application/json")
                        .content("{\"qr\":\"" + qr + "\",\"cnpj\":\"12345678000199\"}")
                        .header("Authorization", "Bearer " + tokenOutraOcs))
                .andExpect(status().isForbidden());
        mvc.perform(post("/atendimentos/" + id + "/sessoes").contentType("application/json")
                        .content("{\"cnpj\":\"12345678000199\"}")
                        .header("Authorization", "Bearer " + tokenOutraOcs))
                .andExpect(status().isForbidden());
        assertEquals(0, qtdRealizada());
    }

    @Test
    void statusExpiradaRealizadaEFaturadaNaoRecebemBaixaNemSessao() throws Exception {
        for (String estado : List.of("EXPIRADA", "REALIZADA", "FATURADA")) {
            jdbc.update("UPDATE guia_proc SET status=? WHERE id=?", estado, id);
            baixar(token).andExpect(status().isConflict());
            assertEquals(409, registrarSessao().getResponse().getStatus());
            mvc.perform(get("/atendimentos/qrcode/" + qr).header("Authorization", "Bearer " + token))
                    .andExpect(status().isConflict());
            assertEquals(0, qtdRealizada());
        }
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM sessao_procedimento WHERE guia_proc_id=?", Integer.class, id));
    }

    @Test
    void guiaExpiradaPreservaSessoesSemPermitirBaixaOuCobranca() throws Exception {
        assertEquals(201, registrarSessao().getResponse().getStatus());
        jdbc.update("UPDATE guia_proc SET data_validade=CURRENT_DATE-1 WHERE id=?", id);
        baixar(token).andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("GUIA_EXPIRADA"));
        assertEquals(409, registrarSessao().getResponse().getStatus());
        assertEquals(1, qtdRealizada());
        mvc.perform(post("/espelhos").contentType("application/json")
                        .content("{\"ocsId\":\"12345678000199\",\"dataInicio\":\"" + java.time.LocalDate.now().withDayOfMonth(1)
                                + "\",\"dataFim\":\"" + java.time.LocalDate.now() + "\",\"itens\":[{\"atendimentoId\":" + id + ",\"valor\":150}]}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM espelho_item WHERE atendimento_id=?", Integer.class, id));
    }

    @Test
    void naoAutenticadoEPerfilBeneficiarioNaoPodemBaixar() throws Exception {
        mvc.perform(post("/atendimentos/validar-qrcode").contentType("application/json").content("{\"qr\":\"" + qr + "\"}"))
                .andExpect(status().isUnauthorized());
        String beneficiario = loginService.entrar(new LoginRequest("99351819019", "fusex123")).token();
        baixar(beneficiario).andExpect(status().isForbidden());
    }

    @Test
    void duasBaixasSimultaneasCriamUmaUnicaSessao() throws Exception {
        jdbc.update("UPDATE guia_proc SET qtd_autorizada=1 WHERE id=?", id);
        var largada = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var tarefas = java.util.stream.IntStream.range(0, 2).mapToObj(i -> executor.submit(() -> {
                assertTrue(largada.await(10, TimeUnit.SECONDS));
                return baixar(token).andReturn().getResponse().getStatus();
            })).toList();
            largada.countDown();
            var resultados = new java.util.ArrayList<Integer>();
            for (var tarefa : tarefas) resultados.add(tarefa.get(20, TimeUnit.SECONDS));
            assertEquals(List.of(200, 409), resultados.stream().sorted().toList());
        }
        assertEquals(1, qtdRealizada());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM sessao_procedimento WHERE guia_proc_id=?", Integer.class, id));
    }

    @Test
    void guiaMarcadaExpiradaBloqueiaMesmoComDataFutura() throws Exception {
        jdbc.update("UPDATE guia SET status='EXPIRADA' WHERE id=?", guiaId);
        baixar(token).andExpect(status().isConflict());
        assertEquals(409, registrarSessao().getResponse().getStatus());
        assertEquals(0, qtdRealizada());
    }

    @Test
    void prazoDaGuiaBloqueiaMesmoComValidadeDoProcedimentoFutura() throws Exception {
        jdbc.update("UPDATE guia SET data_emissao=CURRENT_DATE-61 WHERE id=?", guiaId);
        baixar(token).andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("GUIA_EXPIRADA"));
        assertEquals(409, registrarSessao().getResponse().getStatus());
        assertEquals(0, qtdRealizada());
    }

    @Test
    void corpoSemQrOuComQrEmBrancoRecebe400() throws Exception {
        for (String corpo : List.of("{}", "{\"qr\":\"   \"}")) {
            mvc.perform(post("/atendimentos/validar-qrcode").contentType("application/json")
                            .content(corpo).header("Authorization", "Bearer " + token))
                    .andExpect(status().isBadRequest());
        }
        assertEquals(0, qtdRealizada());
    }

    @Test
    void baixaAntesDasSessoesCompletasNaoAlteraBanco() throws Exception {
        baixar(token).andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.codigo").value("SESSOES_INCOMPLETAS"));
        assertEquals(0, qtdRealizada());
    }

    private org.springframework.test.web.servlet.ResultActions baixar(String autenticacao) throws Exception {
        return mvc.perform(post("/atendimentos/validar-qrcode").contentType("application/json")
                .content("{\"qr\":\"" + qr + "\"}").header("Authorization", "Bearer " + autenticacao));
    }

    private org.springframework.test.web.servlet.MvcResult registrarSessao() throws Exception {
        return mvc.perform(post("/atendimentos/" + id + "/sessoes").contentType("application/json")
                .content("{\"cnpj\":\"98765432000111\"}").header("Authorization", "Bearer " + token)).andReturn();
    }

    private int qtdRealizada() {
        return jdbc.queryForObject("SELECT qtd_realizada FROM guia_proc WHERE id=?", Integer.class, id);
    }
}
