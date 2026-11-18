package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.auth.dto.LoginRequest;
import com.datasphere.fusex.auth.service.LoginService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.docker.compose.enabled=false",
        "spring.datasource.url=${US0081_TEST_DB_URL}",
        "spring.datasource.username=fusex_test",
        "spring.datasource.password=fusex_test_local",
        "spring.jpa.hibernate.ddl-auto=none",
        "logging.level.root=WARN",
        "debug=false"
})
@ActiveProfiles("dev")
@EnabledIfEnvironmentVariable(named = "US0081_TEST_DB_URL", matches = ".+")
class PreGuiaCadastroIntegrationTest {

    private static final String PEDIDO = """
            {"beneficiarioPrecCp":"123456789-00","medicoId":1,"procedimentos":[
              {"procedimentoId":1,"ocsCnpj":"12345678000199","quantidade":2}]}
            """;

    @Autowired private WebApplicationContext contexto;
    @Autowired private FilterChainProxy seguranca;
    @Autowired private LoginService loginService;
    @Autowired private JdbcTemplate jdbc;
    private MockMvc mvc;
    private String emissor;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).addFilters(seguranca).build();
        emissor = login("18609139034");
    }

    @Test
    void criacaoRetornaSomenteIdStatusEPersisteNaListagem() throws Exception {
        var resultado = postar(PEDIDO).andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.status").value("PRE_GUIA")).andReturn();
        long id = ((Number) JsonPath.read(resultado.getResponse().getContentAsString(), "$.id")).longValue();
        assertThat(resultado.getResponse().getHeader("Location")).isEqualTo("/pre-guias/" + id);
        mvc.perform(get("/pre-guias").header("Authorization", "Bearer " + emissor))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id == " + id + ")]").isNotEmpty());
        assertThat(jdbc.queryForObject("SELECT emissor_id FROM pre_guia WHERE id=?", Integer.class, id)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT qtd_autorizada FROM pre_guia_proc WHERE pre_guia_id=?", Integer.class, id))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject("""
                SELECT origem FROM historico_status WHERE entidade_tipo='PRE_GUIA'
                AND entidade_id=? ORDER BY data, id LIMIT 1
                """, String.class, id)).isEqualTo("criacao-pre-guia");
    }

    @Test
    void editarSubstituiItensBeneficiarioEMedicoSemDuplicar() throws Exception {
        long id = criar();
        String pedido = """
                {"beneficiarioPrecCp":"111111111-00","medicoId":2,"procedimentos":[
                  {"procedimentoId":2,"ocsCnpj":"98765432000111","quantidade":3},
                  {"procedimentoId":4,"ocsCnpj":"11222333000144","quantidade":1}]}
                """;
        editar(id, pedido).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PRE_GUIA"));
        editar(id, pedido).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM pre_guia_proc WHERE pre_guia_id=?", Integer.class, id))
                .isEqualTo(2);
        mvc.perform(get("/pre-guias/" + id).header("Authorization", "Bearer " + emissor))
                .andExpect(status().isOk()).andExpect(jsonPath("$.beneficiario.precCp").value("111111111-00"))
                .andExpect(jsonPath("$.medico.id").value(2))
                .andExpect(jsonPath("$.procedimentos.length()").value(2));
    }

    @Test
    void edicaoComPendenciasRetornaParaPreGuia() throws Exception {
        long id = criar();
        jdbc.update("UPDATE pre_guia SET status='COM_PENDENCIAS' WHERE id=?", id);
        editar(id, PEDIDO).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PRE_GUIA"));
        assertThat(jdbc.queryForObject("SELECT status FROM pre_guia WHERE id=?", String.class, id)).isEqualTo("PRE_GUIA");
    }

    @Test
    void validacaoImpedeEdicaoEConservaOsItens() throws Exception {
        long id = criar();
        mvc.perform(post("/pre-guias/" + id + "/validar").header("Authorization", "Bearer " + emissor))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("VALIDADA"));
        var itens = jdbc.queryForList("SELECT id FROM pre_guia_proc WHERE pre_guia_id=?", Long.class, id);
        editar(id, PEDIDO).andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("PRE_GUIA_STATUS_INVALIDO"));
        assertThat(jdbc.queryForList("SELECT id FROM pre_guia_proc WHERE pre_guia_id=?", Long.class, id)).isEqualTo(itens);
    }

    @Test
    void todosOsStatusNaoEditaveisRetornam409() throws Exception {
        long id = criar();
        for (String estado : List.of("VALIDADA", "APROVADA", "REPROVADA", "EMITIDA")) {
            jdbc.update("UPDATE pre_guia SET status=? WHERE id=?", estado, id);
            editar(id, PEDIDO).andExpect(status().isConflict())
                    .andExpect(jsonPath("$.codigo").value("PRE_GUIA_STATUS_INVALIDO"));
        }
    }

    @Test
    void duplicidadeESemContratoNaoCriamPreGuiaParcial() throws Exception {
        int antes = quantidadePreGuias();
        String duplicado = PEDIDO.replace("\"quantidade\":2}",
                "\"quantidade\":2},{\"procedimentoId\":1,\"ocsCnpj\":\"12345678000199\",\"quantidade\":1}");
        postar(duplicado).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("PROCEDIMENTO_REPETIDO"));
        postar(PEDIDO.replace("\"procedimentoId\":1", "\"procedimentoId\":6"))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.codigo").value("PROCEDIMENTO_SEM_CONTRATO"));
        assertThat(quantidadePreGuias()).isEqualTo(antes);
    }

    @Test
    void edicaoInvalidaPreservaBeneficiarioItensEHistorico() throws Exception {
        long id = criar();
        var antes = jdbc.queryForList("SELECT id, proc_id, ocs_id, qtd_autorizada FROM pre_guia_proc WHERE pre_guia_id=?", id);
        editar(id, PEDIDO.replace("\"procedimentoId\":1", "\"procedimentoId\":6"))
                .andExpect(status().isUnprocessableEntity());
        assertThat(jdbc.queryForList("SELECT id, proc_id, ocs_id, qtd_autorizada FROM pre_guia_proc WHERE pre_guia_id=?", id))
                .isEqualTo(antes);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM historico_status WHERE entidade_tipo='PRE_GUIA' AND entidade_id=?",
                Integer.class, id)).isEqualTo(1);
    }

    @Test
    void camposObrigatoriosListaVaziaItemNuloEQuantidadeInvalidaRetornam400() throws Exception {
        for (String pedido : List.of("{}", "{\"beneficiarioPrecCp\":\"123456789-00\",\"medicoId\":1,\"procedimentos\":[]}",
                "{\"beneficiarioPrecCp\":\"123456789-00\",\"medicoId\":1,\"procedimentos\":[null]}",
                PEDIDO.replace("\"quantidade\":2", "\"quantidade\":0"),
                PEDIDO.replace("\"quantidade\":2", "\"quantidade\":null"),
                PEDIDO.replace("\"medicoId\":1", "\"medicoId\":0"),
                PEDIDO.replace("12345678000199", "invalid"))) {
            postar(pedido).andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("VALIDACAO"));
        }
    }

    @Test
    void medicoRecebe403ESemTokenRecebe401() throws Exception {
        String medico = login("08301661305");
        mvc.perform(post("/pre-guias").contentType("application/json").content(PEDIDO)
                        .header("Authorization", "Bearer " + medico)).andExpect(status().isForbidden());
        mvc.perform(put("/pre-guias/1").contentType("application/json").content(PEDIDO)
                        .header("Authorization", "Bearer " + medico)).andExpect(status().isForbidden());
        mvc.perform(post("/pre-guias").contentType("application/json").content(PEDIDO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recursoAusenteRetorna404() throws Exception {
        editar(999999999L, PEDIDO).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PRE_GUIA_NAO_ENCONTRADA"));
    }

    @Test
    void primeiroEventoDaPreGuiaContinuaCriacaoDepoisDaEmissao() throws Exception {
        long id = criar();
        mvc.perform(post("/pre-guias/" + id + "/validar").header("Authorization", "Bearer " + emissor))
                .andExpect(status().isOk());
        mvc.perform(patch("/pre-guias/" + id + "/aprovar").header("Authorization", "Bearer " + login("52601815906")))
                .andExpect(status().isOk());
        mvc.perform(post("/pre-guias/" + id + "/emitir").header("Authorization", "Bearer " + emissor))
                .andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT guia_id FROM pre_guia WHERE id=?", Long.class, id)).isNotNull();
        assertThat(jdbc.queryForList("""
                SELECT origem FROM historico_status WHERE entidade_tipo='PRE_GUIA' AND entidade_id=? ORDER BY data, id
                """, String.class, id)).startsWith("criacao-pre-guia").endsWith("emissao-da-guia");
    }

    private int quantidadePreGuias() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM pre_guia", Integer.class);
    }

    private String login(String cpf) {
        return loginService.entrar(new LoginRequest(cpf, "fusex123")).token();
    }

    private ResultActions postar(String corpo) throws Exception {
        return mvc.perform(post("/pre-guias").header("Authorization", "Bearer " + emissor)
                .contentType("application/json").content(corpo));
    }

    private ResultActions editar(long id, String corpo) throws Exception {
        return mvc.perform(put("/pre-guias/" + id).header("Authorization", "Bearer " + emissor)
                .contentType("application/json").content(corpo));
    }

    private long criar() throws Exception {
        String json = postar(PEDIDO).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }
}
