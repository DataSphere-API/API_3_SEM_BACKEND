package com.datasphere.fusex.config.security;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.auth.service.TokenService;
import com.datasphere.fusex.config.CorsConfig;
import com.datasphere.fusex.guia.controller.ExpiracaoController;
import com.datasphere.fusex.guia.service.ExpirarGuiasService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.EnumSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExpiracaoController.class)
@Import({SecurityConfig.class, RespostaErroSeguranca.class, TokenService.class, CorsConfig.class})
class RotasAdministrativasTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private ExpirarGuiasService expirarGuiasService;

    private String tokenCom(Perfil perfil) {
        UsuarioLogado usuario = new UsuarioLogado(1, "52601815906", "Teste", EnumSet.of(perfil),
                null, null, null, null, null, null, null, null);
        return "Bearer " + tokenService.gerar(usuario);
    }

    @Test
    void expirarSemToken_devolve401() throws Exception {
        mockMvc.perform(post("/admin/guias/expirar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expirarComoEmissor_devolve403() throws Exception {
        mockMvc.perform(post("/admin/guias/expirar").header(HttpHeaders.AUTHORIZATION, tokenCom(Perfil.EMISSOR)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
    }

    @Test
    void expirarComoChefe_passa() throws Exception {
        mockMvc.perform(post("/admin/guias/expirar").header(HttpHeaders.AUTHORIZATION, tokenCom(Perfil.CHEFE)))
                .andExpect(status().isOk());
    }

    @Test
    void rotaInexistente_devolve404EmJson() throws Exception {
        mockMvc.perform(get("/api/hello").header(HttpHeaders.AUTHORIZATION, tokenCom(Perfil.CHEFE)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
    }
}
