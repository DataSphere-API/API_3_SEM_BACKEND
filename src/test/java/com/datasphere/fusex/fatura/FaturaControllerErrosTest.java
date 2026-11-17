package com.datasphere.fusex.fatura;

import com.datasphere.fusex.exception.GlobalExceptionHandler;
import com.datasphere.fusex.fatura.service.BuscarFaturaPorIdService;
import com.datasphere.fusex.fatura.service.ListarFaturasPorStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FaturaControllerErrosTest {

    @Mock private ListarFaturasPorStatusService listarFaturasPorStatusService;
    @Mock private BuscarFaturaPorIdService buscarFaturaPorIdService;
    @Mock private ItemFaturaRepository itemFaturaRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FaturaController controller = new FaturaController(
                listarFaturasPorStatusService,
                buscarFaturaPorIdService,
                itemFaturaRepository
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
}
