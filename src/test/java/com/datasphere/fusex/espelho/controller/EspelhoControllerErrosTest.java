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
    @Mock private AprovarEspelhoService aprovarEspelhoService;
    @Mock private ReprovarEspelhoService reprovarEspelhoService;
    @Mock private ReenviarEspelhoService reenviarEspelhoService;
    @Mock private com.datasphere.fusex.auditor.service.IdentificarAuditorService identificarAuditorService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        EspelhoController controller = new EspelhoController(validarAtendimentoRealizadoService,
                validarEspelhoNaoDuplicadoPorAtendimentoService, criarEspelhoComItensService, listarEspelhosService,
                listarEspelhosPorOcsService, listarEspelhosPorStatusService, buscarEspelhoPorIdService,
                listarItensDoEspelhoService, corrigirItemEspelhoService, removerItemDoEspelhoService,
                enriquecerListaEspelhosService, aprovarEspelhoService, reprovarEspelhoService, reenviarEspelhoService,
                identificarAuditorService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void espelhoSemItens_devolve422() throws Exception {
        when(criarEspelhoComItensService.criarEspelhoComItens(any(EspelhoRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.ESPELHO_SEM_ITENS, List.of("itens: o espelho deve conter pelo menos um item")));

        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"atendimentoId\":1,\"ocsId\":\"12345678000199\",\"itens\":[]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("ESPELHO_SEM_ITENS"));
    }

    @Test
    void itemComValorNegativo_devolve400ComOCampo() throws Exception {
        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO.replace("150.00", "-1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value("itens[0].valor: deve ser maior que zero"));
    }

    @Test
    void atendimentoNaoRealizado_devolve400EmJson() throws Exception {
        when(criarEspelhoComItensService.criarEspelhoComItens(any(EspelhoRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.ATENDIMENTO_NAO_REALIZADO));

        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ATENDIMENTO_NAO_REALIZADO"));
    }

    @Test
    void espelhoDuplicado_devolve409() throws Exception {
        when(criarEspelhoComItensService.criarEspelhoComItens(any(EspelhoRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.ATENDIMENTO_JA_EM_ESPELHO));

        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ATENDIMENTO_JA_EM_ESPELHO"));
    }

    @Test
    void ocsDivergente_devolve422EmJson() throws Exception {
        when(criarEspelhoComItensService.criarEspelhoComItens(any(EspelhoRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.OCS_DIVERGENTE, List.of("ocsId: a OCS informada não corresponde à OCS do contrato do atendimento")));

        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("OCS_DIVERGENTE"));
    }

    @Test
    void beneficiarioForaDaGuia_devolve422EmJson() throws Exception {
        when(criarEspelhoComItensService.criarEspelhoComItens(any(EspelhoRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.BENEFICIARIO_FORA_DA_GUIA, List.of("itens[0].beneficiarioId: beneficiário não pertence à guia do atendimento")));

        mockMvc.perform(post("/espelhos").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("BENEFICIARIO_FORA_DA_GUIA"));
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
