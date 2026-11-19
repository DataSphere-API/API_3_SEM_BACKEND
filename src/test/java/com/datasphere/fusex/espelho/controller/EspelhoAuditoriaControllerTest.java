package com.datasphere.fusex.espelho.controller;

import com.datasphere.fusex.auditor.service.IdentificarAuditorService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.DecisaoEspelhoResponse;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EspelhoAuditoriaControllerTest {

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
    @Mock private IdentificarAuditorService identificarAuditorService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        EspelhoController controller = new EspelhoController(
                validarAtendimentoRealizadoService,
                validarEspelhoNaoDuplicadoPorAtendimentoService,
                criarEspelhoComItensService,
                listarEspelhosService,
                listarEspelhosPorOcsService,
                listarEspelhosPorStatusService,
                buscarEspelhoPorIdService,
                listarItensDoEspelhoService,
                corrigirItemEspelhoService,
                removerItemDoEspelhoService,
                enriquecerListaEspelhosService,
                aprovarEspelhoService,
                reprovarEspelhoService,
                reenviarEspelhoService,
                identificarAuditorService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void aprovarEspelho_sucessoDevolve200ComDecisao() throws Exception {
        EspelhoModel espelho = new EspelhoModel();
        espelho.setId(1);
        espelho.setStatus(StatusEspelho.EM_ABERTO);

        when(buscarEspelhoPorIdService.buscarEspelhoPorId(1)).thenReturn(espelho);
        when(identificarAuditorService.identificarAuditor(2)).thenReturn(2);
        when(aprovarEspelhoService.aprovarEspelho(1, 2))
                .thenReturn(new DecisaoEspelhoResponse(1, StatusEspelho.APROVADO, null, 2));

        mockMvc.perform(patch("/espelhos/1/aprovar").header("X-Auditor-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.motivo").doesNotExist())
                .andExpect(jsonPath("$.auditorId").value(2));
    }

    @Test
    void reprovarEspelho_sucessoDevolve200ComDecisao() throws Exception {
        EspelhoModel espelho = new EspelhoModel();
        espelho.setId(1);
        espelho.setStatus(StatusEspelho.EM_ABERTO);

        when(buscarEspelhoPorIdService.buscarEspelhoPorId(1)).thenReturn(espelho);
        when(identificarAuditorService.identificarAuditor(2)).thenReturn(2);
        when(reprovarEspelhoService.reprovarEspelho(1, 2, "Valor acima do contrato"))
                .thenReturn(new DecisaoEspelhoResponse(1, StatusEspelho.REPROVADO, "Valor acima do contrato", 2));

        mockMvc.perform(patch("/espelhos/1/reprovar")
                        .header("X-Auditor-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Valor acima do contrato\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("REPROVADO"))
                .andExpect(jsonPath("$.motivo").value("Valor acima do contrato"))
                .andExpect(jsonPath("$.auditorId").value(2));
    }

    @Test
    void reenviarEspelho_sucessoDevolve200ComDecisao() throws Exception {
        EspelhoModel espelho = new EspelhoModel();
        espelho.setId(1);
        espelho.setStatus(StatusEspelho.REPROVADO);

        when(buscarEspelhoPorIdService.buscarEspelhoPorId(1)).thenReturn(espelho);
        when(reenviarEspelhoService.reenviarEspelho(1))
                .thenReturn(new DecisaoEspelhoResponse(1, StatusEspelho.EM_ABERTO, null, null));

        mockMvc.perform(post("/espelhos/1/reenviar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("EM_ABERTO"))
                .andExpect(jsonPath("$.motivo").doesNotExist())
                .andExpect(jsonPath("$.auditorId").doesNotExist());
    }

    @Test
    void espelhoInexistente_devolve404() throws Exception {
        when(buscarEspelhoPorIdService.buscarEspelhoPorId(99)).thenReturn(null);

        mockMvc.perform(patch("/espelhos/99/aprovar").header("X-Auditor-Id", 2))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ESPELHO_NAO_ENCONTRADO"));

        mockMvc.perform(patch("/espelhos/99/reprovar").header("X-Auditor-Id", 2))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ESPELHO_NAO_ENCONTRADO"));

        mockMvc.perform(post("/espelhos/99/reenviar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ESPELHO_NAO_ENCONTRADO"));
    }

    @Test
    void auditorInexistente_devolve403() throws Exception {
        EspelhoModel espelho = new EspelhoModel();
        espelho.setId(1);
        when(buscarEspelhoPorIdService.buscarEspelhoPorId(1)).thenReturn(espelho);
        when(identificarAuditorService.identificarAuditor(99))
                .thenThrow(new BusinessException(ErrorCode.AUDITOR_INVALIDO));

        mockMvc.perform(patch("/espelhos/1/aprovar").header("X-Auditor-Id", 99))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("AUDITOR_INVALIDO"));

        mockMvc.perform(patch("/espelhos/1/reprovar").header("X-Auditor-Id", 99))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("AUDITOR_INVALIDO"));
    }

    @Test
    void reprovarSemMotivo_devolve400() throws Exception {
        EspelhoModel espelho = new EspelhoModel();
        espelho.setId(1);
        when(buscarEspelhoPorIdService.buscarEspelhoPorId(1)).thenReturn(espelho);
        when(identificarAuditorService.identificarAuditor(2)).thenReturn(2);
        when(reprovarEspelhoService.reprovarEspelho(1, 2, null))
                .thenThrow(new BusinessException(ErrorCode.MOTIVO_OBRIGATORIO));

        mockMvc.perform(patch("/espelhos/1/reprovar")
                        .header("X-Auditor-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("MOTIVO_OBRIGATORIO"));
    }

    @Test
    void decidirDuasVezes_devolve409() throws Exception {
        EspelhoModel espelho = new EspelhoModel();
        espelho.setId(1);
        espelho.setStatus(StatusEspelho.APROVADO);
        when(buscarEspelhoPorIdService.buscarEspelhoPorId(1)).thenReturn(espelho);
        when(identificarAuditorService.identificarAuditor(2)).thenReturn(2);
        when(aprovarEspelhoService.aprovarEspelho(1, 2))
                .thenThrow(new BusinessException(ErrorCode.ESPELHO_JA_AUDITADO));

        mockMvc.perform(patch("/espelhos/1/aprovar").header("X-Auditor-Id", 2))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESPELHO_JA_AUDITADO"));
    }

    @Test
    void reenviarEspelhoNaoReprovado_devolve409() throws Exception {
        EspelhoModel espelho = new EspelhoModel();
        espelho.setId(1);
        espelho.setStatus(StatusEspelho.EM_ABERTO);
        when(buscarEspelhoPorIdService.buscarEspelhoPorId(1)).thenReturn(espelho);
        when(reenviarEspelhoService.reenviarEspelho(1))
                .thenThrow(new BusinessException(ErrorCode.ESPELHO_NAO_REPROVADO));

        mockMvc.perform(post("/espelhos/1/reenviar"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESPELHO_NAO_REPROVADO"));
    }
}
