package com.datasphere.fusex.historicostatus.service;

import com.datasphere.fusex.auth.dto.LoginRequest;
import com.datasphere.fusex.auth.dto.LoginResponse;
import com.datasphere.fusex.auth.service.LoginService;
import com.datasphere.fusex.guia.service.ExpiracaoAgendada;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.docker.compose.enabled=false",
        "spring.datasource.url=${US0071_TEST_DB_URL}",
        "spring.datasource.username=fusex_test",
        "spring.datasource.password=fusex_test_local",
        "spring.jpa.hibernate.ddl-auto=none",
        "logging.level.root=WARN",
        "debug=false"
})
@ActiveProfiles("dev")
@EnabledIfEnvironmentVariable(named = "US0071_TEST_DB_URL", matches = ".+")
@Transactional
class UsuarioNoHistoricoIntegrationTest {

    @Autowired private WebApplicationContext contexto;
    @Autowired private FilterChainProxy seguranca;
    @Autowired private LoginService loginService;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ExpiracaoAgendada expiracaoAgendada;
    @Autowired private ListarHistoricoPorEntidadeService listarHistorico;
    private MockMvc mvc;
    private long preGuiaId;

    @BeforeEach
    void preparar() {
        SecurityContextHolder.clearContext();
        mvc = MockMvcBuilders.webAppContextSetup(contexto).addFilters(seguranca).build();
        preGuiaId = jdbc.queryForObject("""
                INSERT INTO pre_guia (status, benf_id, med_id, emissor_id, data_criacao)
                VALUES ('VALIDADA', '123456789-00', 1, 1, CURRENT_DATE) RETURNING id
                """, Long.class);
        jdbc.update("""
                INSERT INTO pre_guia_proc (pre_guia_id, proc_id, ocs_id, qtd_autorizada)
                VALUES (?, 1, '12345678000199', 2), (?, 4, '11222333000144', 1)
                """, preGuiaId, preGuiaId);
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void aprovacaoEEmissaoGravamOsUsuariosReaisENomesNoDto() throws Exception {
        var chefe = login("52601815906");
        var emissor = login("18609139034");
        aprovar(chefe);
        var resposta = mvc.perform(post("/pre-guias/" + preGuiaId + "/emitir").header("Authorization", bearer(emissor)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long guiaId = ((Number) JsonPath.read(resposta, "$.guiaId")).longValue();
        conferirUsuario("PRE_GUIA", preGuiaId, "aprovacao-chefe", chefe);
        conferirUsuario("PRE_GUIA", preGuiaId, "emissao-da-guia", emissor);
        conferirUsuario("GUIA", guiaId, "emissao-da-guia", emissor);
        var eventos = listarHistorico.executar(EntidadeTipo.GUIA, guiaId).stream().map(HistoricoStatusResponse::from).toList();
        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0).usuarioId()).isEqualTo(emissor.usuario().id());
        assertThat(eventos.get(0).usuarioNome()).isEqualTo(emissor.usuario().nome());
    }

    @Test
    void auditorAprovaEspelhoEHistoricoHttpDevolveNomeEId() throws Exception {
        long guiaId = criarGuia(false);
        long atendimento = criarProcedimento(guiaId);
        jdbc.update("UPDATE guia_proc SET status='REALIZADA', qtd_realizada=1 WHERE id=?", atendimento);
        long espelho = jdbc.queryForObject("""
                INSERT INTO espelho (atendimento_id, ocs_id, data_inicio, data_fim, status)
                VALUES (?, '12345678000199', CURRENT_DATE, CURRENT_DATE, 'EM_ABERTO') RETURNING id
                """, Long.class, atendimento);
        var auditor = login("99603082430");
        mvc.perform(patch("/espelhos/" + espelho + "/aprovar").header("Authorization", bearer(auditor)))
                .andExpect(status().isOk());
        mvc.perform(get("/espelhos/" + espelho + "/historico").header("Authorization", bearer(auditor)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].usuarioId").value(auditor.usuario().id()))
                .andExpect(jsonPath("$[0].usuarioNome").value(auditor.usuario().nome()))
                .andExpect(jsonPath("$[0].statusNovo").value("APROVADO"));
    }

    @Test
    void expiracaoManualGravaChefeNaGuiaENoProcedimento() throws Exception {
        long guiaId = criarGuia(true);
        long procedimento = criarProcedimento(guiaId);
        var chefe = login("52601815906");
        mvc.perform(post("/admin/guias/expirar").header("Authorization", bearer(chefe)))
                .andExpect(status().isOk());
        conferirUsuario("GUIA", guiaId, "expiracao-de-guias", chefe);
        conferirUsuario("GUIA_PROC", procedimento, "expiracao-de-guias", chefe);
    }

    @Test
    void jobSemLoginGravaExpiracaoSemUsuario() {
        long guiaId = criarGuia(true);
        long procedimento = criarProcedimento(guiaId);
        expiracaoAgendada.executarTodaNoite();
        var eventosDaGuia = listarHistorico.executar(EntidadeTipo.GUIA, guiaId);
        assertThat(eventosDaGuia).hasSize(1);
        for (var evento : eventosDaGuia) {
            assertThat(evento.getUsuarioId()).isNull();
            assertThat(evento.getUsuarioNome()).isNull();
        }
        var eventos = listarHistorico.executar(EntidadeTipo.GUIA_PROC, procedimento);
        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0).getStatusNovo()).isEqualTo("EXPIRADA");
        assertThat(eventos.get(0).getUsuarioId()).isNull();
        assertThat(eventos.get(0).getUsuarioNome()).isNull();
    }

    @Test
    void nomeHistoricoNaoMudaQuandoCadastroEAlterado() throws Exception {
        var chefe = login("52601815906");
        aprovar(chefe);
        jdbc.update("UPDATE beneficiario SET nome='Nome alterado no cadastro' WHERE preccp='444444444-00'");
        conferirUsuario("PRE_GUIA", preGuiaId, "aprovacao-chefe", chefe);
    }

    @Test
    void nomeCompostoDaOcsEGravadoSemTruncar() throws Exception {
        jdbc.update("UPDATE ocs SET nome=? WHERE cnpj='12345678000199'", "N".repeat(150));
        jdbc.update("UPDATE funocs SET cargo=? WHERE ocs_id='12345678000199'", "C".repeat(80));
        var ocs = login("62819482112");
        assertThat(ocs.usuario().nome()).hasSize(233);
        long guiaId = criarGuia(false);
        long procedimento = criarProcedimento(guiaId);
        jdbc.update("UPDATE guia_proc SET data_validade=CURRENT_DATE+60 WHERE id=?", procedimento);
        mvc.perform(post("/atendimentos/" + procedimento + "/sessoes")
                        .header("Authorization", bearer(ocs)).contentType("application/json")
                        .content("{\"cnpj\":\"12345678000199\"}"))
                .andExpect(status().isCreated());
        conferirUsuario("GUIA_PROC", procedimento, "registro-sessao", ocs);
    }

    @Test
    void formatoAntigoDeEventoContinuaValidoComCamposNulos() {
        long guiaId = criarGuia(false);
        jdbc.update("""
                INSERT INTO historico_status (entidade_tipo, entidade_id, status_novo, data, origem)
                VALUES ('GUIA', ?, 'EMITIDA', CURRENT_TIMESTAMP, 'evento-antigo')
                """, guiaId);
        var eventos = listarHistorico.executar(EntidadeTipo.GUIA, guiaId);
        assertThat(eventos).hasSize(1);
        var resposta = HistoricoStatusResponse.from(eventos.get(0));
        assertThat(resposta.usuarioId()).isNull();
        assertThat(resposta.usuarioNome()).isNull();
    }

    private void aprovar(LoginResponse chefe) throws Exception {
        mvc.perform(patch("/pre-guias/" + preGuiaId + "/aprovar").header("Authorization", bearer(chefe)))
                .andExpect(status().isOk());
    }

    private void conferirUsuario(String tipo, long id, String origem, LoginResponse usuario) {
        var evento = jdbc.queryForMap("""
                SELECT usuario_id, usuario_nome FROM historico_status
                WHERE entidade_tipo=? AND entidade_id=? AND origem=?
                """, tipo, id, origem);
        assertThat(evento.get("usuario_id")).isEqualTo(usuario.usuario().id());
        assertThat(evento.get("usuario_nome")).isEqualTo(usuario.usuario().nome());
    }

    private long criarGuia(boolean vencida) {
        return jdbc.queryForObject("""
                INSERT INTO guia (benf_id, status, data_emissao)
                VALUES ('123456789-00', 'EMITIDA', ?) RETURNING id
                """, Long.class, java.time.LocalDate.now().minusDays(vencida ? 70 : 1));
    }

    private long criarProcedimento(long guiaId) {
        return jdbc.queryForObject("""
                INSERT INTO guia_proc (cont_id, guia_id, status, qtd_autorizada, qtd_realizada, data_validade)
                VALUES (1, ?, 'PENDENTE', 1, 0, CURRENT_DATE - 1) RETURNING id
                """, Long.class, guiaId);
    }

    private LoginResponse login(String cpf) {
        return loginService.entrar(new LoginRequest(cpf, "fusex123"));
    }

    private String bearer(LoginResponse usuario) {
        return "Bearer " + usuario.token();
    }
}
