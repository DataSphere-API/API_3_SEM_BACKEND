package com.datasphere.fusex.linhadotempo.controller;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.exception.GlobalExceptionHandler;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.linhadotempo.dto.EtapaLinhaDoTempo;
import com.datasphere.fusex.linhadotempo.dto.EventoLinhaDoTempoResponse;
import com.datasphere.fusex.linhadotempo.dto.LinhaDoTempoResponse;
import com.datasphere.fusex.linhadotempo.service.MontarLinhaDoTempoDaGuiaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LinhaDoTempoControllerTest {

    @Mock
    private MontarLinhaDoTempoDaGuiaService montarLinhaDoTempoDaGuiaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new LinhaDoTempoController(montarLinhaDoTempoDaGuiaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void guiaExistente_retorna200ComEventos() throws Exception {
        EventoLinhaDoTempoResponse evento = new EventoLinhaDoTempoResponse(
                EtapaLinhaDoTempo.EMISSAO, EntidadeTipo.GUIA, 3L, null, "EMITIDA",
                "Guia emitida", LocalDateTime.of(2026, 11, 10, 9, 0), "emissao-da-guia");
        when(montarLinhaDoTempoDaGuiaService.montar(3L))
                .thenReturn(new LinhaDoTempoResponse(3L, "EMITIDA", LocalDate.of(2026, 11, 10), List.of(evento)));

        mockMvc.perform(get("/guias/3/linha-do-tempo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guiaId").value(3))
                .andExpect(jsonPath("$.statusAtual").value("EMITIDA"))
                .andExpect(jsonPath("$.eventos[0].etapa").value("EMISSAO"))
                .andExpect(jsonPath("$.eventos[0].descricao").value("Guia emitida"));
    }

    @Test
    void guiaInexistente_retorna404() throws Exception {
        when(montarLinhaDoTempoDaGuiaService.montar(99L))
                .thenThrow(new BusinessException(ErrorCode.GUIA_NAO_ENCONTRADA));

        mockMvc.perform(get("/guias/99/linha-do-tempo"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("GUIA_NAO_ENCONTRADA"));
    }
}