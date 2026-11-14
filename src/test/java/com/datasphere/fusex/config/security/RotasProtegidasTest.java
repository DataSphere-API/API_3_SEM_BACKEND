package com.datasphere.fusex.config.security;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.auth.controller.AuthController;
import com.datasphere.fusex.auth.service.CadastrarBeneficiarioService;
import com.datasphere.fusex.auth.service.LoginService;
import com.datasphere.fusex.auth.service.TokenService;
import com.datasphere.fusex.config.CorsConfig;
import com.datasphere.fusex.guia.controller.BeneficiarioGuiaController;
import com.datasphere.fusex.guia.service.GuiaBeneficiarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.EnumSet;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Sobe só a camada web com o SecurityConfig de verdade (sem banco).
@WebMvcTest(controllers = {BeneficiarioGuiaController.class, AuthController.class})
@Import({SecurityConfig.class, RespostaErroSeguranca.class, TokenService.class, CorsConfig.class})
class RotasProtegidasTest {

    private static final String ROTA = "/beneficiarios/123456789-00/guias";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private GuiaBeneficiarioService guiaBeneficiarioService;
    @MockitoBean
    private LoginService loginService;
    @MockitoBean
    private CadastrarBeneficiarioService cadastrarBeneficiarioService;

    private String tokenCom(Perfil... perfis) {
        UsuarioLogado usuario = new UsuarioLogado(1, "99351819019", "Teste", EnumSet.of(perfis[0], perfis),
                "123456789-00", null, null, null, null, null, null, null);
        return "Bearer " + tokenService.gerar(usuario);
    }

    @Test
    void semToken_devolve401NoFormatoPadrao() throws Exception {
        mockMvc.perform(get(ROTA))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
    }

    @Test
    void tokenInventado_devolve401() throws Exception {
        mockMvc.perform(get(ROTA).header(HttpHeaders.AUTHORIZATION, "Bearer inventado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void perfilErrado_devolve403() throws Exception {
        mockMvc.perform(get(ROTA).header(HttpHeaders.AUTHORIZATION, tokenCom(Perfil.OCS)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
    }

    @Test
    void perfilCerto_passa() throws Exception {
        when(guiaBeneficiarioService.listarGuiasPorBeneficiario("123456789-00")).thenReturn(List.of());

        mockMvc.perform(get(ROTA).header(HttpHeaders.AUTHORIZATION, tokenCom(Perfil.BENEFICIARIO)))
                .andExpect(status().isOk());
    }

    @Test
    void loginEPublico() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"99351819019\",\"senha\":\"fusex123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void preflightDoFront_passaSemToken() throws Exception {
        mockMvc.perform(options(ROTA)
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }
}