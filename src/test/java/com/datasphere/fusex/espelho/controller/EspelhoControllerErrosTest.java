package com.datasphere.fusex.espelho.controller;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.dto.EspelhoRequest;
import com.datasphere.fusex.espelho.service.*;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EspelhoControllerErrosTest {

    private static final String CORPO_VALIDO = "{\"ocsId\":\"12345678000199\",\"dataInicio\":\"2026-11-01\","
            + "\"dataFim\":\"2026-11-15\",\"itens\":[{\"atendimentoId\":12,\"valor\":150.00}]}";

    @Mock private CriarEspelhoComItensService criarEspelhoComItensService;
    @Mock private ListarEspelhosService listarEspelhosService;
    @Mock private BuscarEspelhoPorIdService buscarEspelhoPorIdService;
    @Mock private ListarItensDoEspelhoService listarItensDoEspelhoService;
    @Mock private CorrigirItemEspelhoService corrigirItemEspelhoService;
    @Mock private RemoverItemDoEspelhoService removerItemDoEspelhoService;
    @Mock private EnriquecerListaEspelhosService enriquecerListaEspelhosService;
    @Mock private AprovarEspelhoService aprovarEspelhoService;
    @Mock private ReprovarEspelhoService reprovarEspelhoService;
    @Mock private ReenviarEspelhoService reenviarEspelhoService;
    @Mock private com.datasphere.fusex.auditor.service.IdentificarAuditorService identificarAuditorService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        EspelhoController controller = new EspelhoController(criarEspelhoComItensService, listarEspelhosService,
                buscarEspelhoPorIdService, listarItensDoEspelhoService, corrigirItemEspelhoService,
                removerItemDoEspelhoService, enriquecerListaEspelhosService, aprovarEspelhoService,
                reprovarEspelhoService, reenviarEspelhoService, identificarAuditorService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private void criarFalhaCom(BusinessException erro) {
        when(criarEspelhoComItensService.criarEspelhoComItens(any(EspelhoRequest.class))).thenThrow(erro);
    }

    @Test
    void semAtendimentos_devolve400ComOCampo() throws Exception {
        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO.replace("[{\"atendimentoId\":12,\"valor\":150.00}]", "[]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value("itens: escolha pelo menos um atendimento"));
        verifyNoInteractions(criarEspelhoComItensService);
    }

    @Test
    void itemComValorNegativo_devolve400ComOCampo() throws Exception {
        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO.replace("150.00", "-1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value("itens[0].valor: deve ser maior que zero"));
    }

    @Test
    void semPeriodo_devolve400() throws Exception {
        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itens\":[{\"atendimentoId\":12,\"valor\":150.00}]}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(criarEspelhoComItensService);
    }

    @Test
    void espelhoCriado_devolve201() throws Exception {
        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isCreated());
    }

    @Test
    void atendimentoNaoRealizado_devolve422() throws Exception {
        criarFalhaCom(new BusinessException(ErrorCode.ATENDIMENTO_NAO_REALIZADO, List.of("atendimento 12")));

        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("ATENDIMENTO_NAO_REALIZADO"));
    }

    @Test
    void foraDoMes_devolve422() throws Exception {
        criarFalhaCom(new BusinessException(ErrorCode.FORA_DO_MES));

        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("FORA_DO_MES"));
    }

    @Test
    void foraDoPeriodo_devolve422() throws Exception {
        criarFalhaCom(new BusinessException(ErrorCode.FORA_DO_PERIODO, List.of("atendimento 12")));

        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("FORA_DO_PERIODO"));
    }

    @Test
    void atendimentoJaEmEspelho_devolve409() throws Exception {
        criarFalhaCom(new BusinessException(ErrorCode.ATENDIMENTO_JA_EM_ESPELHO, List.of("atendimento 12 já está no espelho #3")));

        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ATENDIMENTO_JA_EM_ESPELHO"));
    }

    @Test
    void espelhoInexistente_devolve404EmJson() throws Exception {
        when(buscarEspelhoPorIdService.buscarEspelhoVisivel(99))
                .thenThrow(new BusinessException(ErrorCode.ESPELHO_NAO_ENCONTRADO));

        mockMvc.perform(get("/espelhos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ESPELHO_NAO_ENCONTRADO"))
                .andExpect(jsonPath("$.mensagem").value("Espelho não encontrado."));
    }

    @Test
    void espelhoDeOutraOcs_devolve403() throws Exception {
        when(buscarEspelhoPorIdService.buscarEspelhoVisivel(5))
                .thenThrow(new BusinessException(ErrorCode.OCS_DIFERENTE_DO_CONTRATO));

        mockMvc.perform(get("/espelhos/5/itens"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("OCS_DIFERENTE_DO_CONTRATO"));
        verifyNoInteractions(listarItensDoEspelhoService);
    }

    @Test
    void corrigirItemSemValor_devolve400() throws Exception {
        mockMvc.perform(patch("/espelhos/1/itens/2").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(corrigirItemEspelhoService);
    }

    @Test
    void corrigirItem_devolve204() throws Exception {
        mockMvc.perform(patch("/espelhos/1/itens/2").contentType(MediaType.APPLICATION_JSON).content("{\"valor\":120.00}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removerItemUltimoItem_devolve422() throws Exception {
        when(removerItemDoEspelhoService.removerItemDoEspelho(1, 2))
                .thenThrow(new BusinessException(ErrorCode.ESPELHO_SEM_ITENS, List.of("itens: não é permitido remover o último item do espelho")));

        mockMvc.perform(delete("/espelhos/1/itens/2"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("ESPELHO_SEM_ITENS"));
    }

    @Test
    void removerItemEspelhoCancelado_devolve409() throws Exception {
        when(removerItemDoEspelhoService.removerItemDoEspelho(1, 2))
                .thenThrow(new BusinessException(ErrorCode.ESPELHO_CANCELADO));

        mockMvc.perform(delete("/espelhos/1/itens/2"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESPELHO_CANCELADO"));
    }

    @Test
    void removerItemNaoEncontrado_devolve404() throws Exception {
        when(removerItemDoEspelhoService.removerItemDoEspelho(1, 2))
                .thenThrow(new BusinessException(ErrorCode.ITEM_NAO_ENCONTRADO));

        mockMvc.perform(delete("/espelhos/1/itens/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ITEM_NAO_ENCONTRADO"));
    }
}
