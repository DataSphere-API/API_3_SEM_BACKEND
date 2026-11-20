package com.datasphere.fusex.guia.controller;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.exception.GlobalExceptionHandler;
import com.datasphere.fusex.guia.dto.DocumentoGuia;
import com.datasphere.fusex.guia.service.BuscarDocumentoDaGuiaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GuiaDocumentoControllerTest {

    private static final String HASH = "c".repeat(64);
    private static final byte[] PDF = "%PDF-1.4 guia".getBytes();

    @Mock
    private BuscarDocumentoDaGuiaService buscarDocumentoDaGuiaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new GuiaDocumentoController(buscarDocumentoDaGuiaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void guiaComDocumento_devolvePdfComHashNoCabecalho() throws Exception {
        when(buscarDocumentoDaGuiaService.buscar(3L)).thenReturn(new DocumentoGuia(PDF, HASH));

        mockMvc.perform(get("/guias/3/documento"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("X-Hash-Pacote", HASH))
                .andExpect(header().string("Content-Disposition", "inline; filename=guia-3.pdf"))
                .andExpect(content().bytes(PDF));
    }

    @Test
    void guiaSemDocumento_devolve404EmJson() throws Exception {
        when(buscarDocumentoDaGuiaService.buscar(1L))
                .thenThrow(new BusinessException(ErrorCode.DOCUMENTO_GUIA_INDISPONIVEL));

        mockMvc.perform(get("/guias/1/documento"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("DOCUMENTO_GUIA_INDISPONIVEL"));
    }
}
