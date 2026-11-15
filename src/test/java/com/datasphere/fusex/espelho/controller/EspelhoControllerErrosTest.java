package com.datasphere.fusex.espelho.controller;

import com.datasphere.fusex.espelho.service.*;
import com.datasphere.fusex.exception.GlobalExceptionHandler;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EspelhoControllerErrosTest {

    private static final String CORPO_VALIDO = "{\"atendimentoId\":1,\"ocsId\":\"12345678000199\","
            + "\"itens\":[{\"beneficiarioId\":\"123456789-00\",\"descricao\":\"Psicoterapia\",\"valor\":150.00}]}";

    @Mock private ValidarAtendimentoRealizadoService validarAtendimentoRealizadoService;
    @Mock private ValidarEspelhoNaoDuplicadoPorAtendimentoService validarEspelhoNaoDuplicadoPorAtendimentoService;
    @Mock private CriarEspelhoComItensService criarEspelhoComItensService;
    @Mock private ListarEspelhosService listarEspelhosService;
    @Mock private ListarEspelhosPorOcsService listarEspelhosPorOcsService;
    @Mock private ListarEspelhosPorStatusService listarEspelhosPorStatusService;
    @Mock private BuscarEspelhoPorIdService buscarEspelhoPorIdService;
    @Mock private ListarItensDoEspelhoService listarItensDoEspelhoService;
    @Mock private CorrigirItemEspelhoService corrigirItemEspelhoService;
    @Mock private RemoverItemDoEspelhoService removerItemDoEspelhoService;
    @Mock private EnriquecerListaEspelhosService enriquecerListaEspelhosService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        EspelhoController controller = new EspelhoController(validarAtendimentoRealizadoService,
                validarEspelhoNaoDuplicadoPorAtendimentoService, criarEspelhoComItensService, listarEspelhosService,
                listarEspelhosPorOcsService, listarEspelhosPorStatusService, buscarEspelhoPorIdService,
                listarItensDoEspelhoService, corrigirItemEspelhoService, removerItemDoEspelhoService,
                enriquecerListaEspelhosService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void espelhoSemItens_devolve400AntesDeChamarOService() throws Exception {
        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"atendimentoId\":1,\"ocsId\":\"12345678000199\",\"itens\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                .andExpect(jsonPath("$.detalhes[0]").value("itens: não pode ser vazio"));
        verifyNoInteractions(validarAtendimentoRealizadoService, criarEspelhoComItensService);
    }

    @Test
    void itemComValorNegativo_devolve400ComOCampo() throws Exception {
        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO.replace("150.00", "-1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value("itens[0].valor: deve ser maior que zero"));
    }

    @Test
    void atendimentoNaoRealizado_devolve422EmJson() throws Exception {
        when(validarAtendimentoRealizadoService.validarAtendimentoRealizado(1L)).thenReturn(null);

        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("ATENDIMENTO_NAO_REALIZADO"));
    }

    @Test
    void atendimentoJaEmEspelho_devolve409() throws Exception {
        when(validarAtendimentoRealizadoService.validarAtendimentoRealizado(1L)).thenReturn(new GuiaProcModel());
        when(validarEspelhoNaoDuplicadoPorAtendimentoService.validarEspelhoNaoDuplicadoPorAtendimento(1L)).thenReturn(false);

        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ATENDIMENTO_JA_EM_ESPELHO"));
    }

    @Test
    void espelhoInexistente_devolve404EmJson() throws Exception {
        when(buscarEspelhoPorIdService.buscarEspelhoPorId(99)).thenReturn(null);

        mockMvc.perform(get("/espelhos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ESPELHO_NAO_ENCONTRADO"))
                .andExpect(jsonPath("$.mensagem").value("Espelho não encontrado."));
    }

    @Test
    void removerItemQueNaoPode_devolve422() throws Exception {
        when(removerItemDoEspelhoService.removerItemDoEspelho(1, 2)).thenReturn(false);

        mockMvc.perform(delete("/espelhos/1/itens/2"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("ITEM_ESPELHO_NAO_REMOVIDO"));
    }
}
