package com.datasphere.fusex.atendimento.controller;

import com.datasphere.fusex.atendimento.service.GarantirQrDoProcedimentoService;
import com.datasphere.fusex.atendimento.service.GerarImagemQrCodeService;
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

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class QrCodeControllerTest {

    @Mock private GarantirQrDoProcedimentoService garantirQrDoProcedimentoService;
    @Mock private GerarImagemQrCodeService gerarImagemQrCodeService;
    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        mvc = MockMvcBuilders.standaloneSetup(new QrCodeController(garantirQrDoProcedimentoService,
                        gerarImagemQrCodeService)).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void devolveSomenteODtoComOsCamposDoContrato() throws Exception {
        String codigo = "7f3c9e2a-e94b-4a50-a0f7-b5bf9402ad18";
        when(garantirQrDoProcedimentoService.garantirQrDoProcedimento(10L)).thenReturn(codigo);
        when(gerarImagemQrCodeService.gerarImagemQrCode(codigo)).thenReturn("data:image/png;base64,iVBOR...");
        mvc.perform(get("/atendimentos/10/qrcode"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$.atendimentoId").value(10))
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.imagemBase64").value("data:image/png;base64,iVBOR..."));
    }

    @ParameterizedTest
    @EnumSource(value = ErrorCode.class, names = {"ATENDIMENTO_NAO_ENCONTRADO", "GUIA_EXPIRADA", "PROCEDIMENTO_JA_REALIZADO", "GUIA_NAO_EMITIDA"})
    void errosDeNegocioMantemStatusECodigoSemGerarImagem(ErrorCode codigo) throws Exception {
        when(garantirQrDoProcedimentoService.garantirQrDoProcedimento(10L)).thenThrow(new BusinessException(codigo));
        mvc.perform(get("/atendimentos/10/qrcode"))
                .andExpect(status().is(codigo.getStatus().value()))
                .andExpect(jsonPath("$.codigo").value(codigo.getCodigo()));
        verifyNoInteractions(gerarImagemQrCodeService);
    }
}
