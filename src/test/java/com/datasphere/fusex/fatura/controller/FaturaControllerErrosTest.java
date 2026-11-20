package com.datasphere.fusex.fatura.controller;
import com.datasphere.fusex.fatura.ItemFaturaRepository;

import com.datasphere.fusex.exception.GlobalExceptionHandler;
import com.datasphere.fusex.fatura.service.BuscarFaturaPorIdService;
import com.datasphere.fusex.fatura.service.ListarFaturasMensaisService;
import com.datasphere.fusex.fatura.service.MontarFaturaMensalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FaturaControllerErrosTest {

    @Mock private ListarFaturasMensaisService listarFaturasMensaisService;
    @Mock private BuscarFaturaPorIdService buscarFaturaPorIdService;
    @Mock private MontarFaturaMensalService montarFaturaMensalService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FaturaController controller = new FaturaController(
                listarFaturasMensaisService,
                buscarFaturaPorIdService,
                montarFaturaMensalService
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void faturaInexistente_devolve404EmJson() throws Exception {
        when(buscarFaturaPorIdService.buscarFaturaPorId(9L)).thenReturn(null);

        mockMvc.perform(get("/faturas/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("FATURA_NAO_ENCONTRADA"));
    }

    @Test
    void listarRepassaOsFiltros() throws Exception {
        when(listarFaturasMensaisService.listar("12345678000199", "2026-10", null)).thenReturn(List.of());

        mockMvc.perform(get("/faturas").param("cnpj", "12345678000199").param("competencia", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(listarFaturasMensaisService).listar("12345678000199", "2026-10", null);
    }
}
