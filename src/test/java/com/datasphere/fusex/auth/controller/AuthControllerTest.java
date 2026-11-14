package com.datasphere.fusex.auth.controller;

import com.datasphere.fusex.auth.dto.CadastroRequest;
import com.datasphere.fusex.auth.dto.CadastroResponse;
import com.datasphere.fusex.auth.dto.LoginRequest;
import com.datasphere.fusex.auth.dto.LoginResponse;
import com.datasphere.fusex.auth.dto.UsuarioResponse;
import com.datasphere.fusex.auth.service.CadastrarBeneficiarioService;
import com.datasphere.fusex.auth.service.LoginService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private LoginService loginService;
    @Mock
    private CadastrarBeneficiarioService cadastrarBeneficiarioService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(loginService, cadastrarBeneficiarioService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void login_devolveOContratoDoCard() throws Exception {
        UsuarioResponse usuario = new UsuarioResponse(2, "Carlos Medeiros", "08301661305",
                List.of("MEDICO", "BENEFICIARIO"), "222222222-00", "SP123456", 1, null, null);
        when(loginService.entrar(any(LoginRequest.class))).thenReturn(new LoginResponse("abc", usuario));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"08301661305\",\"senha\":\"fusex123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("abc"))
                .andExpect(jsonPath("$.usuario.id").value(2))
                .andExpect(jsonPath("$.usuario.nome").value("Carlos Medeiros"))
                .andExpect(jsonPath("$.usuario.cpf").value("08301661305"))
                .andExpect(jsonPath("$.usuario.perfis[0]").value("MEDICO"))
                .andExpect(jsonPath("$.usuario.precCp").value("222222222-00"))
                .andExpect(jsonPath("$.usuario.crm").value("SP123456"))
                .andExpect(jsonPath("$.usuario.medicoId").value(1))
                .andExpect(jsonPath("$.usuario.ocsCnpj").value(nullValue()))
                .andExpect(jsonPath("$.usuario.cargo").value(nullValue()));
    }

    @Test
    void loginErrado_devolve401ComCodigo() throws Exception {
        when(loginService.entrar(any(LoginRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.CREDENCIAIS_INVALIDAS));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"08301661305\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIAIS_INVALIDAS"))
                .andExpect(jsonPath("$.mensagem").value("CPF ou senha incorretos."));
    }

    @Test
    void cadastro_devolve201() throws Exception {
        when(cadastrarBeneficiarioService.cadastrar(any(CadastroRequest.class)))
                .thenReturn(new CadastroResponse(7, "Maria Silva", "777777777-00"));

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Maria Silva\",\"precCp\":\"777777777-00\",\"dataNascimento\":\"1990-01-10\","
                                + "\"dependente\":false,\"cpfResponsavel\":null,\"cpf\":\"52998224725\",\"senha\":\"senhaForte1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.precCp").value("777777777-00"));
    }

    @Test
    void cadastroComRegraQuebrada_devolve422ComCodigo() throws Exception {
        when(cadastrarBeneficiarioService.cadastrar(any(CadastroRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.PREC_CP_NAO_CONFERE));

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"João Silva\",\"precCp\":\"987654321-01\",\"dataNascimento\":\"2005-03-01\","
                                + "\"dependente\":true,\"cpfResponsavel\":\"52601815906\",\"cpf\":\"11144477735\",\"senha\":\"senhaForte1\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("PREC_CP_NAO_CONFERE"));
    }
}