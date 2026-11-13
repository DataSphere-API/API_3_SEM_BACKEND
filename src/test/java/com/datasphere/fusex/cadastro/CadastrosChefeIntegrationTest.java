package com.datasphere.fusex.cadastro;

import com.datasphere.fusex.beneficiario.controller.BeneficiarioController;
import com.datasphere.fusex.config.CadastrosMockFilter;
import com.datasphere.fusex.contrato.controller.ContratoController;
import com.datasphere.fusex.exception.GlobalExceptionHandler;
import com.datasphere.fusex.medico.controller.MedicoController;
import com.datasphere.fusex.ocs.controller.OcsController;
import com.datasphere.fusex.procedimento.controller.ProcedimentoController;
import com.datasphere.fusex.usuario.controller.UsuarioController;
import com.datasphere.fusex.ocs.OcsRepository;
import com.datasphere.fusex.procedimento.ProcedimentoRepository;
import com.datasphere.fusex.procedimento.ProcedimentoModel;
import com.datasphere.fusex.contrato.ContratoRepository;
import com.datasphere.fusex.contrato.ContratoModel;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.docker.compose.enabled=false",
        "spring.datasource.url=${US00017_TEST_DB_URL}",
        "spring.datasource.username=fusex_test",
        "spring.datasource.password=fusex_test_local",
        "spring.jpa.hibernate.ddl-auto=none",
        "logging.level.root=WARN",
        "debug=false"
})
@ActiveProfiles({"dev", "cadastros-mock"})
@EnabledIfEnvironmentVariable(named = "US00017_TEST_DB_URL", matches = ".+")
@Transactional
class CadastrosChefeIntegrationTest {

    @Autowired private OcsController ocsController;
    @Autowired private ProcedimentoController procedimentoController;
    @Autowired private ContratoController contratoController;
    @Autowired private UsuarioController usuarioController;
    @Autowired private BeneficiarioController beneficiarioController;
    @Autowired private MedicoController medicoController;
    @Autowired private GlobalExceptionHandler exceptionHandler;
    @Autowired private CadastrosMockFilter mockFilter;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private OcsRepository ocsRepository;
    @Autowired private ProcedimentoRepository procedimentoRepository;
    @Autowired private ContratoRepository contratoRepository;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.standaloneSetup(ocsController, procedimentoController, contratoController,
                        usuarioController, beneficiarioController, medicoController)
                .setControllerAdvice(exceptionHandler).addFilters(mockFilter).build();
    }

    private org.springframework.test.web.servlet.ResultActions cadastrar(String caminho, String corpo) throws Exception {
        return mvc.perform(post(caminho).header("X-Mock-Login", "00000000001")
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    @Test
    void somenteChefePodeCadastrar() throws Exception {
        for (String caminho : new String[]{"/ocs", "/procedimentos", "/contratos", "/usuarios"}) {
            mvc.perform(post(caminho).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("AUTENTICACAO_OBRIGATORIA"));
            mvc.perform(post(caminho).header("X-Mock-Login", "00000000002")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("CADASTRO_RESTRITO_AO_CHEFE"));
        }
        mvc.perform(get("/usuarios")).andExpect(status().isUnauthorized());
    }

    @Test
    void ocsGravaNormalizaCnpjERejeitaDuplicidadeSemAlterarNome() throws Exception {
        cadastrar("/ocs", "{\"cnpj\":\"12.345.678/0001-99\",\"nome\":\"Outro nome\"}")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("OCS_JA_CADASTRADA"));
        assertEquals("Clínica X", jdbc.queryForObject("SELECT nome FROM ocs WHERE cnpj='12345678000199'", String.class));
        cadastrar("/ocs", "{\"cnpj\":\"99887766000155\",\"nome\":\" OCS Nova \"}")
                .andExpect(status().isCreated()).andExpect(jsonPath("$.nome").value("OCS Nova"));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM ocs WHERE cnpj='99887766000155'", Integer.class));
        mvc.perform(get("/ocs")).andExpect(status().isOk()).andExpect(jsonPath("$.ocs").isArray());
    }

    @Test
    void procedimentoGravaENomeEUnicoSemDiferenciarMaiusculas() throws Exception {
        cadastrar("/procedimentos", "{\"nome\":\"  TC Abdome Superior  \"}")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("PROCEDIMENTO_JA_CADASTRADO"));
        cadastrar("/procedimentos", "{\"nome\":\"tc abdome superior\"}")
                .andExpect(status().isConflict());
        String nome = "Procedimento " + UUID.randomUUID();
        cadastrar("/procedimentos", "{\"nome\":\"" + nome + "\"}").andExpect(status().isCreated());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM procedimento WHERE nome=?", Integer.class, nome));
        mvc.perform(get("/procedimentos")).andExpect(status().isOk()).andExpect(jsonPath("$.procedimentos").isArray());
    }

    @Test
    void contratoGravaEDevolveNomesEValor() throws Exception {
        cadastrar("/contratos", "{\"ocsCnpj\":\"12345678000199\",\"procedimentoId\":6,\"valor\":720.49}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ocsCnpj").value("12345678000199"))
                .andExpect(jsonPath("$.ocsNome").value("Clínica X"))
                .andExpect(jsonPath("$.procedimentoId").value(6))
                .andExpect(jsonPath("$.procedimentoNome").value("Ressonância magnética de joelho"))
                .andExpect(jsonPath("$.valor").value(720.49));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM contrato WHERE proc_id=6 AND ocs_id='12345678000199'", Integer.class));
        mvc.perform(get("/contratos")).andExpect(status().isOk())
                .andExpect(jsonPath("$.contratos").isArray())
                .andExpect(jsonPath("$.contratos[0].ocsNome").isString())
                .andExpect(jsonPath("$.contratos[0].procedimentoNome").isString());
    }

    @Test
    void contratoRejeitaDuplicidadeValoresInvalidosEVinculosAusentes() throws Exception {
        cadastrar("/contratos", "{\"ocsCnpj\":\"12345678000199\",\"procedimentoId\":1,\"valor\":20}")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CONTRATO_DUPLICADO"));
        for (String valor : new String[]{"0", "-1", "null", "0.001", "100000000"}) {
            cadastrar("/contratos", "{\"ocsCnpj\":\"12345678000199\",\"procedimentoId\":1,\"valor\":" + valor + "}")
                    .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.codigo").value("VALOR_INVALIDO"));
        }
        cadastrar("/contratos", "{\"ocsCnpj\":\"00000000000000\",\"procedimentoId\":1,\"valor\":10}")
                .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.codigo").value("OCS_NAO_ENCONTRADA"));
        cadastrar("/contratos", "{\"ocsCnpj\":\"12345678000199\",\"procedimentoId\":999999,\"valor\":10}")
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("PROCEDIMENTO_NAO_ENCONTRADO"));
    }

    @Test
    void buscaBeneficiariosPorNomeOuPrecCpLimitaVinteComCamposDoContrato() throws Exception {
        for (int i = 0; i < 25; i++) {
            jdbc.update("INSERT INTO beneficiario(preccp, nome, dependente, cotista) VALUES (?, ?, FALSE, FALSE)",
                    "US17-LIMITE-" + i, "Teste limite " + i);
        }
        mvc.perform(get("/beneficiarios").param("busca", "TESTE LIMITE"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.beneficiarios.length()").value(20));
        mvc.perform(get("/beneficiarios").param("busca", "US17-MILITAR"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.beneficiarios.length()").value(1))
                .andExpect(jsonPath("$.beneficiarios[0].precCp").value("US17-MILITAR"))
                .andExpect(jsonPath("$.beneficiarios[0].nome").value("Militar com autocadastro simulado"))
                .andExpect(jsonPath("$.beneficiarios[0].dataNascimento").value("1986-05-10"))
                .andExpect(jsonPath("$.beneficiarios[0].dependente").value(false))
                .andExpect(jsonPath("$.beneficiarios[0].ug").value("2ª Bda"));
        mvc.perform(get("/beneficiarios").param("busca", "%"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.beneficiarios.length()").value(0));
        mvc.perform(get("/beneficiarios").param("busca", "inexistente"))
                .andExpect(jsonPath("$.beneficiarios").isEmpty());
        mvc.perform(get("/beneficiarios")).andExpect(jsonPath("$.beneficiarios.length()").value(20));
    }

    @Test
    void buscaMedicosIncluiNomeDoBeneficiario() throws Exception {
        mvc.perform(get("/medicos")).andExpect(status().isOk())
                .andExpect(jsonPath("$.medicos[0].id").isNumber())
                .andExpect(jsonPath("$.medicos[0].nome").isString())
                .andExpect(jsonPath("$.medicos[0].crm").isString());
    }

    @Test
    void militarExigeAutocadastroPerfilECrm() throws Exception {
        cadastrar("/usuarios", "{\"tipo\":\"MILITAR\",\"precCp\":\"US17-PENDENTE\",\"perfis\":[\"AUDITOR\"]}")
                .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.codigo").value("PRIMEIRO_ACESSO_PENDENTE"));
        cadastrar("/usuarios", "{\"tipo\":\"MILITAR\",\"precCp\":\"US17-MILITAR\",\"perfis\":[]}")
                .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.codigo").value("PERFIL_OBRIGATORIO"));
        cadastrar("/usuarios", "{\"tipo\":\"MILITAR\",\"precCp\":\"US17-MILITAR\",\"perfis\":[\"MEDICO\"]}")
                .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.codigo").value("MEDICO_SEM_CRM"));
        cadastrar("/usuarios", "{\"tipo\":\"MILITAR\",\"precCp\":\"US17-MILITAR\",\"perfis\":[\"INVALIDO\"]}")
                .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.codigo").value("PERFIL_OBRIGATORIO"));
    }

    @Test
    void militarAcrescentaPerfisSemDuplicarENaoCriaNovoLogin() throws Exception {
        int usuariosAntes = jdbc.queryForObject("SELECT COUNT(*) FROM usuario", Integer.class);
        String corpo = "{\"tipo\":\"MILITAR\",\"precCp\":\"US17-MILITAR\",\"perfis\":[\"EMISSOR\",\"AUDITOR\",\"MEDICO\"],\"crm\":\"US17-CRM-NOVO\"}";
        cadastrar("/usuarios", corpo).andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfis").isArray()).andExpect(jsonPath("$.crm").value("US17-CRM-NOVO"))
                .andExpect(jsonPath("$.senha").doesNotExist());
        cadastrar("/usuarios", corpo).andExpect(status().isCreated());
        assertEquals(usuariosAntes, jdbc.queryForObject("SELECT COUNT(*) FROM usuario", Integer.class));
        for (String tabela : new String[]{"analista", "auditor", "medico"}) {
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM " + tabela + " WHERE benef_id='US17-MILITAR'", Integer.class));
        }
    }

    @Test
    void usuarioOcsGravaSenhaComHashEVinculoSemExporCredencial() throws Exception {
        cadastrar("/usuarios", "{\"tipo\":\"OCS\",\"cpf\":\"999.888.777-66\",\"senha\":\"Teste-US17!\",\"ocsCnpj\":\"12345678000199\",\"cargo\":\"Recepção\"}")
                .andExpect(status().isCreated()).andExpect(jsonPath("$.cpf").value("99988877766"))
                .andExpect(jsonPath("$.perfis[0]").value("OCS"))
                .andExpect(jsonPath("$.senha").doesNotExist());
        String senha = jdbc.queryForObject("SELECT senha FROM usuario WHERE login='99988877766'", String.class);
        assertNotEquals("Teste-US17!", senha);
        assertTrue(new BCryptPasswordEncoder().matches("Teste-US17!", senha));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM funocs f JOIN usuario u ON u.id=f.usuario_id WHERE u.login='99988877766'", Integer.class));
        cadastrar("/usuarios", "{\"tipo\":\"OCS\",\"cpf\":\"99988877766\",\"senha\":\"Teste-US17!\",\"ocsCnpj\":\"12345678000199\"}")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CPF_JA_CADASTRADO"));
        mvc.perform(get("/usuarios").header("X-Mock-Login", "00000000001"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.usuarios").isArray())
                .andExpect(jsonPath("$.usuarios[0].senha").doesNotExist());
    }

    @Test
    void usuarioOcsExigeOcsExistenteERejeitaTipoInvalido() throws Exception {
        cadastrar("/usuarios", "{\"tipo\":\"OCS\",\"cpf\":\"12312312312\",\"senha\":\"Teste-US17!\",\"ocsCnpj\":\"00000000000000\"}")
                .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.codigo").value("OCS_NAO_ENCONTRADA"));
        cadastrar("/usuarios", "{\"tipo\":\"OUTRO\"}").andExpect(status().isBadRequest());
        cadastrar("/ocs", "{\"cnpj\":\"abc\",\"nome\":\"Inválido\"}").andExpect(status().isBadRequest());
        cadastrar("/procedimentos", "{\"nome\":\" \"}").andExpect(status().isBadRequest());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void cadastrosConfirmamTransacoesEConservamDadosDepoisDeErros() throws Exception {
        String cnpj = String.valueOf(java.util.concurrent.ThreadLocalRandom.current()
                .nextLong(10000000000000L, 99999999999999L));
        String nome = "Persistencia " + UUID.randomUUID();
        cadastrar("/ocs", "{\"cnpj\":\"" + cnpj + "\",\"nome\":\"" + nome + "\"}")
                .andExpect(status().isCreated());
        cadastrar("/procedimentos", "{\"nome\":\"" + nome + "\"}")
                .andExpect(status().isCreated());
        Integer procedimentoId = jdbc.queryForObject("SELECT id FROM procedimento WHERE nome=?", Integer.class, nome);
        String contrato = "{\"ocsCnpj\":\"" + cnpj + "\",\"procedimentoId\":" + procedimentoId + ",\"valor\":50.25}";
        cadastrar("/contratos", contrato).andExpect(status().isCreated());
        cadastrar("/contratos", contrato).andExpect(status().isConflict());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM contrato WHERE ocs_id=? AND proc_id=?",
                Integer.class, cnpj, procedimentoId));
        assertEquals(nome, jdbc.queryForObject("SELECT nome FROM ocs WHERE cnpj=?", String.class, cnpj));
        // Remove somente os registros criados neste cenário, inclusive quando a suíte usa o mesmo banco novamente.
        jdbc.update("DELETE FROM contrato WHERE ocs_id=?", cnpj);
        jdbc.update("DELETE FROM procedimento WHERE id=?", procedimentoId);
        jdbc.update("DELETE FROM ocs WHERE cnpj=?", cnpj);
    }

    @Test
    void constraintOcsTambemRetorna409QuandoDuplicidadeChegaAoBanco() {
        var erro = assertThrows(DataIntegrityViolationException.class,
                () -> ocsRepository.inserir("12345678000199", "Nome concorrente"));
        var resposta = exceptionHandler.handleIntegridade(erro);
        assertEquals(409, resposta.getStatusCode().value());
        assertEquals("OCS_JA_CADASTRADA", resposta.getBody().codigo());
    }

    @Test
    void constraintProcedimentoTambemRetorna409QuandoDuplicidadeChegaAoBanco() {
        var erro = assertThrows(DataIntegrityViolationException.class,
                () -> procedimentoRepository.saveAndFlush(new ProcedimentoModel(null, "tc abdome superior")));
        var resposta = exceptionHandler.handleIntegridade(erro);
        assertEquals(409, resposta.getStatusCode().value());
        assertEquals("PROCEDIMENTO_JA_CADASTRADO", resposta.getBody().codigo());
    }

    @Test
    void constraintContratoTambemRetorna409QuandoDuplicidadeChegaAoBanco() {
        var ocs = ocsRepository.findById("12345678000199").orElseThrow();
        var procedimento = procedimentoRepository.findById(1).orElseThrow();
        var erro = assertThrows(DataIntegrityViolationException.class, () -> contratoRepository.saveAndFlush(
                new ContratoModel(null, procedimento, ocs, new java.math.BigDecimal("100.00"))));
        var resposta = exceptionHandler.handleIntegridade(erro);
        assertEquals(409, resposta.getStatusCode().value());
        assertEquals("CONTRATO_DUPLICADO", resposta.getBody().codigo());
    }
}
