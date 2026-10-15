package com.datasphere.fusex.atendimento.controller;

import com.datasphere.fusex.atendimento.service.ListarAtendimentosPorOcs;
import com.datasphere.fusex.atendimento.service.RegistrarRealizacaoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.dto.request.ConferenciaQrResponse;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.exception.GlobalExceptionHandler;
import com.datasphere.fusex.service.ValidacaoQrService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ConferenciaQrControllerTest {

    private static final String CNPJ = "12345678000199";

    @Mock
    private RegistrarRealizacaoService registrarRealizacaoService;

    @Mock
    private ListarAtendimentosPorOcs listarAtendimentosPorOcs;

    @Mock
    private ValidacaoQrService validacaoQrService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AtendimentoController controller = new AtendimentoController(
                registrarRealizacaoService, listarAtendimentosPorOcs, validacaoQrService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void qrValido_retorna200ComJsonDoContrato() throws Exception {
        ConferenciaQrResponse resposta = new ConferenciaQrResponse(
                10L,
                4L,
                new ConferenciaQrResponse.Beneficiario("123456", "Fulano"),
                "Sessão de psicoterapia",
                new ConferenciaQrResponse.Ocs(CNPJ, "Clínica X"),
                10,
                10,
                true,
                LocalDate.of(2026, 12, 19),
                "EM_ANDAMENTO");

        when(validacaoQrService.montarDadosConferenciaQr("QR123", CNPJ)).thenReturn(resposta);

        mockMvc.perform(get("/atendimentos/qrcode/QR123").param("cnpj", CNPJ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atendimentoId").value(10))
                .andExpect(jsonPath("$.guiaId").value(4))
                .andExpect(jsonPath("$.beneficiario.precCp").value("123456"))
                .andExpect(jsonPath("$.beneficiario.nome").value("Fulano"))
                .andExpect(jsonPath("$.procedimento").value("Sessão de psicoterapia"))
                .andExpect(jsonPath("$.ocs.cnpj").value(CNPJ))
                .andExpect(jsonPath("$.ocs.nome").value("Clínica X"))
                .andExpect(jsonPath("$.qtdAutorizada").value(10))
                .andExpect(jsonPath("$.qtdRealizada").value(10))
                .andExpect(jsonPath("$.prontoParaBaixa").value(true))
                .andExpect(jsonPath("$.dataValidade").value("2026-12-19"))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
    }

    @Test
    void qrInexistente_retorna404() throws Exception {
        when(validacaoQrService.montarDadosConferenciaQr("NAOEXISTE", CNPJ))
                .thenThrow(new BusinessException(ErrorCode.QR_NAO_ENCONTRADO));

        mockMvc.perform(get("/atendimentos/qrcode/NAOEXISTE").param("cnpj", CNPJ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("QR_NAO_ENCONTRADO"));
    }

    @Test
    void ocsDiferente_retorna422() throws Exception {
        when(validacaoQrService.montarDadosConferenciaQr("QR123", "99999999000100"))
                .thenThrow(new BusinessException(ErrorCode.OCS_DIFERENTE_DO_CONTRATO));

        mockMvc.perform(get("/atendimentos/qrcode/QR123").param("cnpj", "99999999000100"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("OCS_DIFERENTE_DO_CONTRATO"));
    }

    @Test
    void guiaExpirada_retorna422() throws Exception {
        when(validacaoQrService.montarDadosConferenciaQr("QR123", CNPJ))
                .thenThrow(new BusinessException(ErrorCode.GUIA_EXPIRADA));

        mockMvc.perform(get("/atendimentos/qrcode/QR123").param("cnpj", CNPJ))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("GUIA_EXPIRADA"));
    }

    @Test
    void semCnpj_retorna400() throws Exception {
        mockMvc.perform(get("/atendimentos/qrcode/QR123"))
                .andExpect(status().isBadRequest());
    }
}