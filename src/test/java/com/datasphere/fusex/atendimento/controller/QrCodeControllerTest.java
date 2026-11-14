package com.datasphere.fusex.atendimento.controller;

import com.datasphere.fusex.atendimento.service.BuscarQrDoProcedimentoService;
import com.datasphere.fusex.dto.request.QrCodeResponse;
import com.datasphere.fusex.exception.QrCodeIndisponivelException;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class QrCodeControllerTest {

    @Mock private BuscarQrDoProcedimentoService buscarQrDoProcedimentoService;
    private final Principal principal = () -> "login-teste";
    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.standaloneSetup(new QrCodeController(buscarQrDoProcedimentoService))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void devolveSomenteODtoComOsCamposDoContrato() throws Exception {
        String codigo = "7f3c9e2a-e94b-4a50-a0f7-b5bf9402ad18";
        when(buscarQrDoProcedimentoService.buscarQrDoProcedimento(10L, principal))
                .thenReturn(new QrCodeResponse(codigo));
        mvc.perform(get("/atendimentos/10/qrcode").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.imagemBase64").doesNotExist());
    }

    @ParameterizedTest
    @EnumSource(value = ErrorCode.class, names = {"GUIA_EXPIRADA", "PROCEDIMENTO_JA_REALIZADO", "SESSOES_INCOMPLETAS", "QR_NAO_GERADO"})
    void indisponibilidadeNaConsultaRetorna409(ErrorCode codigo) throws Exception {
        when(buscarQrDoProcedimentoService.buscarQrDoProcedimento(10L, principal))
                .thenThrow(new QrCodeIndisponivelException(codigo));
        mvc.perform(get("/atendimentos/10/qrcode").principal(principal))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value(codigo.getCodigo()));
    }

    @ParameterizedTest
    @EnumSource(value = ErrorCode.class, names = {"AUTENTICACAO_OBRIGATORIA", "BENEFICIARIO_SEM_PERMISSAO", "ATENDIMENTO_NAO_ENCONTRADO"})
    void errosDeIdentidadeENaoEncontradoMantemStatus(ErrorCode codigo) throws Exception {
        when(buscarQrDoProcedimentoService.buscarQrDoProcedimento(10L, principal))
                .thenThrow(new BusinessException(codigo));
        mvc.perform(get("/atendimentos/10/qrcode").principal(principal))
                .andExpect(status().is(codigo.getStatus().value()))
                .andExpect(jsonPath("$.codigo").value(codigo.getCodigo()));
    }
}
