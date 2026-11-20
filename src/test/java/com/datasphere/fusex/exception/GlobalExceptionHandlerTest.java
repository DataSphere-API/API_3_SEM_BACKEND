package com.datasphere.fusex.exception;

import com.datasphere.fusex.domain.enums.ErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    @RestController
    static class ControllerDeTeste {

        record Corpo(@NotEmpty(message = "não pode ser vazio") List<String> itens) {}

        @PostMapping("/teste/corpo")
        void corpo(@Valid @RequestBody Corpo corpo) {
        }

        @GetMapping("/teste/negocio")
        void negocio() {
            throw new BusinessException(ErrorCode.FATURA_NAO_ENCONTRADA);
        }

        @GetMapping("/teste/sem-registro")
        void semRegistro() {
            throw new NoSuchElementException("No value present");
        }

        @GetMapping("/teste/inesperado")
        void inesperado() {
            throw new IllegalStateException("segredo interno");
        }

        @GetMapping("/teste/constraint-conhecida")
        void constraintConhecida() {
            throw new DataIntegrityViolationException("duplicado",
                    new ConstraintViolationException("duplicado", new SQLException(), "uq_usuario_login"));
        }

        @GetMapping("/teste/constraint-desconhecida")
        void constraintDesconhecida() {
            throw new DataIntegrityViolationException("outra",
                    new ConstraintViolationException("outra", new SQLException(), "fk_qualquer"));
        }

        @GetMapping("/teste/numero/{id}")
        void numero(@PathVariable Long id) {
        }
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControllerDeTeste())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listaVazia_devolve400NoContratoDoCard() throws Exception {
        mockMvc.perform(post("/teste/corpo").contentType(MediaType.APPLICATION_JSON).content("{\"itens\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
                .andExpect(jsonPath("$.detalhes[0]").value("itens: não pode ser vazio"));
    }

    @Test
    void jsonMalformado_devolve400() throws Exception {
        mockMvc.perform(post("/teste/corpo").contentType(MediaType.APPLICATION_JSON).content("{itens:"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"));
    }

    @Test
    void tipoErradoNaRota_devolve400() throws Exception {
        mockMvc.perform(get("/teste/numero/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value("id: valor inválido"));
    }

    @Test
    void regraDeNegocio_mantemStatusECodigoDoErrorCode() throws Exception {
        mockMvc.perform(get("/teste/negocio"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("FATURA_NAO_ENCONTRADA"));
    }

    @Test
    void orElseThrowSemTratamento_devolve404() throws Exception {
        mockMvc.perform(get("/teste/sem-registro"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
    }

    @Test
    void metodoErrado_devolve405() throws Exception {
        mockMvc.perform(delete("/teste/negocio"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.codigo").value("METODO_NAO_PERMITIDO"));
    }

    @Test
    void constraintConhecida_viraCodigoDeNegocio() throws Exception {
        mockMvc.perform(get("/teste/constraint-conhecida"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CPF_JA_CADASTRADO"));
    }

    @Test
    void constraintDesconhecida_devolve500Padronizado() throws Exception {
        mockMvc.perform(get("/teste/constraint-desconhecida"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERRO_INTERNO"));
    }

    @Test
    void erroInesperado_devolve500SemPilha() throws Exception {
        mockMvc.perform(get("/teste/inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERRO_INTERNO"))
                .andExpect(content().string(not(containsString("segredo interno"))))
                .andExpect(content().string(not(containsString("IllegalStateException"))));
    }
}
