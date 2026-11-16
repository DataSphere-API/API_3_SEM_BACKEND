package com.datasphere.fusex.fatura;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.service.BuscarEspelhoPorIdService;
import com.datasphere.fusex.exception.GlobalExceptionHandler;
import com.datasphere.fusex.fatura.service.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FaturaControllerErrosTest {

    @Mock private BuscarEspelhoPorIdService buscarEspelhoPorIdService;
    @Mock private ValidarEspelhoFaturavelService validarEspelhoFaturavelService;
    @Mock private GerarFaturaDeEspelhoService gerarFaturaDeEspelhoService;
    @Mock private ListarFaturasPorStatusService listarFaturasPorStatusService;
    @Mock private BuscarFaturaPorIdService buscarFaturaPorIdService;
    @Mock private RegerarFaturaDeEspelhoService regerarFaturaDeEspelhoService;
    @Mock private ItemFaturaRepository itemFaturaRepository;
    @Mock private MontarFaturaResponseService montarFaturaResponseService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FaturaController controller = new FaturaController(buscarEspelhoPorIdService, validarEspelhoFaturavelService,
                gerarFaturaDeEspelhoService, listarFaturasPorStatusService, buscarFaturaPorIdService,
                regerarFaturaDeEspelhoService, itemFaturaRepository, montarFaturaResponseService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void corpoSemEspelhoId_devolve400() throws Exception {
        mockMvc.perform(post("/faturas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value("espelhoId: é obrigatório"));
    }

    @Test
    void espelhoInexistente_devolve404EmJson() throws Exception {
        when(buscarEspelhoPorIdService.buscarEspelhoPorId(5)).thenReturn(null);

        mockMvc.perform(post("/faturas").contentType(MediaType.APPLICATION_JSON).content("{\"espelhoId\":5}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ESPELHO_NAO_ENCONTRADO"));
    }

    @Test
    void espelhoJaFaturado_devolve409() throws Exception {
        EspelhoModel espelho = new EspelhoModel();
        when(buscarEspelhoPorIdService.buscarEspelhoPorId(5)).thenReturn(espelho);
        when(validarEspelhoFaturavelService.validarEspelhoFaturavel(espelho)).thenReturn(false);

        mockMvc.perform(post("/faturas").contentType(MediaType.APPLICATION_JSON).content("{\"espelhoId\":5}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESPELHO_NAO_FATURAVEL"));
    }

    @Test
    void faturaInexistente_devolve404EmJson() throws Exception {
        when(buscarFaturaPorIdService.buscarFaturaPorId(9L)).thenReturn(null);

        mockMvc.perform(get("/faturas/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("FATURA_NAO_ENCONTRADA"));
    }

    @Test
    void regerarSemMudanca_devolve409() throws Exception {
        when(buscarFaturaPorIdService.buscarFaturaPorId(9L)).thenReturn(new FaturaModel());
        when(regerarFaturaDeEspelhoService.regerarFaturaDeEspelho(9L)).thenReturn(null);

        mockMvc.perform(post("/faturas/9/regerar"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("FATURA_NAO_REGERAVEL"));
    }
}
