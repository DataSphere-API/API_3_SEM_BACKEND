package com.datasphere.fusex.guia.service;

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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.docker.compose.enabled=false",
        "spring.datasource.url=${US0072_TEST_DB_URL}",
        "spring.datasource.username=fusex_test",
        "spring.datasource.password=fusex_test_local",
        "spring.jpa.hibernate.ddl-auto=none",
        "logging.level.root=WARN",
        "debug=false"
})
@ActiveProfiles("dev")
@EnabledIfEnvironmentVariable(named = "US0072_TEST_DB_URL", matches = ".+")
@Transactional
class LinhaDoTempoIntegrationTest {

    @Autowired private WebApplicationContext contexto;
    @Autowired private FilterChainProxy seguranca;
    @Autowired private LoginService loginService;
    @Autowired private JdbcTemplate jdbc;
    private MockMvc mvc;
    private long guiaId;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).addFilters(seguranca).build();
        guiaId = jdbc.queryForObject("""
                INSERT INTO guia (benf_id, status, data_emissao)
                VALUES ('123456789-00', 'EMITIDA', CURRENT_DATE) RETURNING id
                """, Long.class);
    }

    @Test
    void fluxoRealReuneValidacaoAprovacaoEmissaoSessoesEBaixaDosProcedimentos() throws Exception {
        long preGuiaId = jdbc.queryForObject("""
                INSERT INTO pre_guia (status, benf_id, med_id, emissor_id, data_criacao)
                VALUES ('PRE_GUIA', '123456789-00', 1, 1, CURRENT_DATE) RETURNING id
                """, Long.class);
        jdbc.update("""
                INSERT INTO pre_guia_proc (pre_guia_id, proc_id, ocs_id, qtd_autorizada)
                VALUES (?, 1, '12345678000199', 2), (?, 4, '11222333000144', 1)
                """, preGuiaId, preGuiaId);
        String emissor = login("18609139034");
        mvc.perform(post("/pre-guias/" + preGuiaId + "/validar").header("Authorization", emissor))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("VALIDADA"));
        mvc.perform(patch("/pre-guias/" + preGuiaId + "/aprovar").header("Authorization", login("08301661305")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APROVADA"));
        var emissao = mvc.perform(post("/pre-guias/" + preGuiaId + "/emitir").header("Authorization", emissor))
                .andExpect(status().isOk()).andReturn();
        long emitida = ((Number) JsonPath.read(emissao.getResponse().getContentAsString(), "$.guiaId")).longValue();
        long primeiro = jdbc.queryForObject("SELECT id FROM guia_proc WHERE guia_id=? AND cont_id=1", Long.class, emitida);
        long segundo = jdbc.queryForObject("SELECT id FROM guia_proc WHERE guia_id=? AND cont_id=5", Long.class, emitida);
        String ocs = login("62819482112");
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/atendimentos/" + primeiro + "/sessoes").header("Authorization", ocs)
                            .contentType("application/json").content("{\"cnpj\":\"12345678000199\"}"))
                    .andExpect(status().isCreated());
        }
        baixar(primeiro, ocs);
        baixar(segundo, login("28675310471"));

        var eventos = consultar(emitida, login("99603082430"));
        assertThat(eventos.stream().map(e -> LocalDateTime.parse((String) e.get("data"))).toList()).isSorted();
        assertThat(statusDe(eventos, "PRE_GUIA", preGuiaId)).containsExactly("VALIDADA", "APROVADA", "EMITIDA");
        assertThat(statusDe(eventos, "GUIA", emitida)).containsExactly("EMITIDA", "FINALIZADA");
        assertThat(statusDe(eventos, "GUIA_PROC", primeiro)).containsExactly("PENDENTE", "EM_ANDAMENTO", "REALIZADA");
        assertThat(statusDe(eventos, "GUIA_PROC", segundo)).containsExactly("PENDENTE", "REALIZADA");
        assertThat(eventos).hasSize(10);
        // O beneficiário dono recebe exatamente os mesmos eventos do auditor.
        assertThat(consultar(emitida, login("99351819019"))).isEqualTo(eventos);
    }

    @Test
    void outroBeneficiarioRecebe403MesmoForjandoPrecCpDoDono() throws Exception {
        mvc.perform(get("/guias/2/linha-do-tempo").header("Authorization", login("99351819019"))
                        .header("X-Prec-Cp", "111111111-00"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("BENEFICIARIO_SEM_PERMISSAO"));
    }

    @Test
    void auditorEmissorEChefePodemConsultarGuiaDeOutroBeneficiario() throws Exception {
        for (String cpf : List.of("99603082430", "18609139034", "52601815906")) {
            mvc.perform(get("/guias/2/linha-do-tempo").header("Authorization", login(cpf)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void guiaInexistenteDevolve404ComCodigoDoContrato() throws Exception {
        mvc.perform(get("/guias/999999999/linha-do-tempo").header("Authorization", login("99603082430")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("GUIA_NAO_ENCONTRADA"));
    }

    @Test
    void semTokenRecebe401EOcsRecebe403() throws Exception {
        mvc.perform(get("/guias/" + guiaId + "/linha-do-tempo")).andExpect(status().isUnauthorized());
        mvc.perform(get("/guias/" + guiaId + "/linha-do-tempo").header("Authorization", login("62819482112")))
                .andExpect(status().isForbidden());
    }

    @Test
    void guiaAntigaSemPreGuiaOuEventosRetornaListaVazia() throws Exception {
        assertThat(consultar(guiaId, login("99351819019"))).isEmpty();
    }

    @Test
    void rotaAntigaRetiradaEHistoricosDeEspelhoEFaturaPreservados() throws Exception {
        String auditor = login("99603082430");
        mvc.perform(get("/guias/1/historico").header("Authorization", auditor)).andExpect(status().isNotFound());
        mvc.perform(get("/espelhos/999/historico").header("Authorization", auditor)).andExpect(status().isOk());
        mvc.perform(get("/faturas/999/historico").header("Authorization", auditor)).andExpect(status().isOk());
    }

    @Test
    void eventosDeOutrasEntidadesNaoVazamNaConsulta() throws Exception {
        jdbc.update("""
                INSERT INTO historico_status (entidade_tipo, entidade_id, status_novo, data, origem)
                VALUES ('PRE_GUIA', ?, 'VALIDADA', CURRENT_TIMESTAMP, 'nao-vinculada'),
                       ('GUIA_PROC', ?, 'REALIZADA', CURRENT_TIMESTAMP, 'nao-vinculado'),
                       ('GUIA', ?, 'EMITIDA', CURRENT_TIMESTAMP, 'vinculada')
                """, guiaId, guiaId, guiaId);
        var eventos = consultar(guiaId, login("99603082430"));
        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0).get("origem")).isEqualTo("vinculada");
    }

    private List<String> statusDe(List<Map<String, Object>> eventos, String tipo, long id) {
        return eventos.stream().filter(e -> tipo.equals(e.get("entidadeTipo"))
                        && ((Number) e.get("entidadeId")).longValue() == id)
                .map(e -> (String) e.get("statusNovo")).toList();
    }

    private List<Map<String, Object>> consultar(long id, String token) throws Exception {
        var resposta = mvc.perform(get("/guias/" + id + "/linha-do-tempo").header("Authorization", token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$");
    }

    private String login(String cpf) {
        return "Bearer " + loginService.entrar(new LoginRequest(cpf, "fusex123")).token();
    }

    private void baixar(long atendimentoId, String token) throws Exception {
        String qr = jdbc.queryForObject("SELECT qr FROM guia_proc WHERE id=?", String.class, atendimentoId);
        mvc.perform(post("/atendimentos/validar-qrcode").header("Authorization", token)
                        .contentType("application/json").content("{\"qr\":\"" + qr + "\"}"))
                .andExpect(status().isOk());
    }
}
