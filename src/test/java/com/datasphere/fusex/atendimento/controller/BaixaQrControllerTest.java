package com.datasphere.fusex.atendimento.controller;

import com.datasphere.fusex.atendimento.service.ListarAtendimentosPorOcs;
import com.datasphere.fusex.atendimento.service.RegistrarRealizacaoService;
import com.datasphere.fusex.atendimento.service.RegistrarSessaoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.dto.request.BaixaResponse;
import com.datasphere.fusex.dto.request.ValidarQrCodeRequest;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.exception.GlobalExceptionHandler;
import com.datasphere.fusex.service.ValidacaoQrService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BaixaQrControllerTest {

    private static final String BODY = "{\"qr\":\"QR1\",\"cnpj\":\"12345678000101\"}";

    @Mock
    private RegistrarRealizacaoService registrarRealizacaoService;

    @Mock
    private ListarAtendimentosPorOcs listarAtendimentosPorOcs;

    @Mock
    private ValidacaoQrService validacaoQrService;

    @Mock
    private RegistrarSessaoService registrarSessaoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AtendimentoController controller = new AtendimentoController(
                registrarRealizacaoService,
                listarAtendimentosPorOcs,
                validacaoQrService,
                registrarSessaoService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void baixaConcluida_retorna200ComTodosOsCamposDoBaixaResponse() throws Exception {
        BaixaResponse resposta = new BaixaResponse(
                10L, "Realizada", 10, 10, LocalDateTime.of(2026, 11, 20, 10, 5), "PARCIALMENTE_REALIZADA");
        when(registrarRealizacaoService.registrarRealizacao(any(ValidarQrCodeRequest.class))).thenReturn(resposta);

        mockMvc.perform(post("/atendimentos/validar-qrcode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atendimentoId").value(10))
                .andExpect(jsonPath("$.statusProcedimento").value("Realizada"))
                .andExpect(jsonPath("$.qtdAutorizada").value(10))
                .andExpect(jsonPath("$.qtdRealizada").value(10))
                .andExpect(jsonPath("$.dataValidacao").exists())
                .andExpect(jsonPath("$.statusGuia").value("PARCIALMENTE_REALIZADA"));
    }

    @Test
    void qrInexistente_retorna404() throws Exception {
        when(registrarRealizacaoService.registrarRealizacao(any(ValidarQrCodeRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.QR_NAO_ENCONTRADO));

        mockMvc.perform(post("/atendimentos/validar-qrcode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("QR_NAO_ENCONTRADO"));
    }

    @Test
    void procedimentoJaRealizado_retorna422() throws Exception {
        when(registrarRealizacaoService.registrarRealizacao(any(ValidarQrCodeRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.PROCEDIMENTO_JA_REALIZADO));

        mockMvc.perform(post("/atendimentos/validar-qrcode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("PROCEDIMENTO_JA_REALIZADO"));
    }

    @Test
    void sessoesIncompletas_retorna422ComDetalhes() throws Exception {
        when(registrarRealizacaoService.registrarRealizacao(any(ValidarQrCodeRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.SESSOES_INCOMPLETAS,
                        Map.of("qtdRealizada", 3, "qtdAutorizada", 10, "saldo", 7)));

        mockMvc.perform(post("/atendimentos/validar-qrcode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("SESSOES_INCOMPLETAS"))
                .andExpect(jsonPath("$.detalhes.qtdRealizada").value(3))
                .andExpect(jsonPath("$.detalhes.qtdAutorizada").value(10))
                .andExpect(jsonPath("$.detalhes.saldo").value(7));
    }
}