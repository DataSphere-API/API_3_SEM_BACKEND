package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.atendimento.service.BuscarOcsAutenticadaService;
import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.EspelhoRequest;
import com.datasphere.fusex.espelho.dto.EspelhoResumoResponse;
import com.datasphere.fusex.espelho.dto.ItemEspelhoRequest;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.service.RegistrarMudancaStatusService;
import com.datasphere.fusex.ocs.OcsModel;
import com.datasphere.fusex.procedimento.ProcedimentoModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CriarEspelhoComItensServiceTest {

    private static final String OCS = "12345678000199";

    @Mock private EspelhoRepository espelhoRepository;
    @Mock private EspelhoItemRepository espelhoItemRepository;
    @Mock private GuiaProcRepository guiaProcRepository;
    @Mock private BuscarOcsAutenticadaService buscarOcsAutenticadaService;
    @Mock private ValidarPeriodoDoEspelhoService validarPeriodoDoEspelhoService;
    @Mock private EnriquecerListaEspelhosService enriquecerListaEspelhosService;
    @Mock private RegistrarMudancaStatusService registrarMudancaStatusService;

    @InjectMocks
    private CriarEspelhoComItensService service;

    private final LocalDate inicio = LocalDate.now().withDayOfMonth(1);
    private final LocalDate fim = LocalDate.now();

    @BeforeEach
    void setUp() {
        when(buscarOcsAutenticadaService.buscarOcsAutenticada()).thenReturn(OCS);
    }

    private GuiaProcModel atendimento(long id, String ocs, String status, String precCp, String procedimento) {
        ProcedimentoModel proc = new ProcedimentoModel();
        proc.setNome(procedimento);
        ContratoModel contrato = new ContratoModel();
        contrato.setOcsModel(new OcsModel(ocs, "Clinica"));
        contrato.setProcedimentoModel(proc);
        GuiaModel guia = new GuiaModel();
        guia.setBenfId(precCp);
        GuiaProcModel atendimento = new GuiaProcModel();
        atendimento.setId(id);
        atendimento.setContratoModel(contrato);
        atendimento.setGuiaModel(guia);
        atendimento.setStatus(status);
        atendimento.setQtdAutorizada(1);
        atendimento.setDataValidacao(fim.atTime(10, 0));
        return atendimento;
    }

    private EspelhoRequest pedido(Long... atendimentos) {
        List<ItemEspelhoRequest> itens = java.util.Arrays.stream(atendimentos)
                .map(id -> new ItemEspelhoRequest(id, new BigDecimal("100.00"))).toList();
        return new EspelhoRequest(OCS, inicio, fim, itens);
    }

    private void salvarDevolvendoId() {
        when(espelhoRepository.save(any(EspelhoModel.class))).thenAnswer(inv -> {
            EspelhoModel espelho = inv.getArgument(0);
            espelho.setId(7);
            return espelho;
        });
        when(enriquecerListaEspelhosService.enriquecer(anyList())).thenReturn(List.of(new EspelhoResumoResponse(
                7, 10L, OCS, inicio, fim, StatusEspelho.EM_ABERTO, null, "Clinica", 1, BigDecimal.TEN, null, 0)));
    }

    @Test
    void espelhoComDoisAtendimentos_criaDoisItensComBeneficiarioEDescricaoDaGuia() {
        when(guiaProcRepository.findById(10L)).thenReturn(Optional.of(atendimento(10L, OCS, "REALIZADA", "123456789-00", "Fisioterapia")));
        when(guiaProcRepository.findById(11L)).thenReturn(Optional.of(atendimento(11L, OCS, "REALIZADA", "987654321-00", "Raio-X")));
        when(espelhoItemRepository.findByAtendimentoIdIn(List.of(10L, 11L))).thenReturn(List.of());
        salvarDevolvendoId();

        service.criarEspelhoComItens(pedido(10L, 11L));

        ArgumentCaptor<EspelhoModel> espelho = ArgumentCaptor.forClass(EspelhoModel.class);
        verify(espelhoRepository).save(espelho.capture());
        assertEquals(OCS, espelho.getValue().getOcsId());
        assertEquals(inicio, espelho.getValue().getDataInicio());
        assertEquals(fim, espelho.getValue().getDataFim());
        assertEquals(10L, espelho.getValue().getAtendimentoId());
        assertEquals(StatusEspelho.EM_ABERTO, espelho.getValue().getStatus());

        ArgumentCaptor<EspelhoItemModel> itens = ArgumentCaptor.forClass(EspelhoItemModel.class);
        verify(espelhoItemRepository, times(2)).save(itens.capture());
        assertEquals("123456789-00", itens.getAllValues().get(0).getBeneficiarioId());
        assertEquals("Fisioterapia", itens.getAllValues().get(0).getDescricao());
        assertEquals(11L, itens.getAllValues().get(1).getAtendimentoId());
        assertEquals("Raio-X", itens.getAllValues().get(1).getDescricao());
        verify(registrarMudancaStatusService).executar(EntidadeTipo.ESPELHO, 7L, null, "EM_ABERTO", "criacao-espelho");
    }

    @Test
    void ocsDoCorpoDiferenteDaLogada_devolve403SemGravar() {
        EspelhoRequest request = new EspelhoRequest("99999999000199", inicio, fim,
                List.of(new ItemEspelhoRequest(10L, BigDecimal.TEN)));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.criarEspelhoComItens(request));
        assertEquals(ErrorCode.OCS_DIFERENTE_DO_CONTRATO, ex.getErrorCode());
        verifyNoInteractions(espelhoRepository, espelhoItemRepository);
    }

    @Test
    void atendimentoDeOutraOcs_devolveOcsDivergente() {
        when(guiaProcRepository.findById(10L)).thenReturn(Optional.of(atendimento(10L, "99999999000199", "REALIZADA", "1", "X")));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.criarEspelhoComItens(pedido(10L)));
        assertEquals(ErrorCode.OCS_DIVERGENTE, ex.getErrorCode());
        verifyNoInteractions(espelhoRepository);
    }

    @Test
    void atendimentoSemBaixaFinal_devolveNaoRealizado() {
        when(guiaProcRepository.findById(10L)).thenReturn(Optional.of(atendimento(10L, OCS, "EM_ANDAMENTO", "1", "X")));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.criarEspelhoComItens(pedido(10L)));
        assertEquals(ErrorCode.ATENDIMENTO_NAO_REALIZADO, ex.getErrorCode());
    }

    @Test
    void baixaFinalForaDoPeriodo_devolveForaDoPeriodo() {
        GuiaProcModel antigo = atendimento(10L, OCS, "REALIZADA", "1", "X");
        antigo.setDataValidacao(inicio.minusDays(1).atTime(9, 0));
        when(guiaProcRepository.findById(10L)).thenReturn(Optional.of(antigo));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.criarEspelhoComItens(pedido(10L)));
        assertEquals(ErrorCode.FORA_DO_PERIODO, ex.getErrorCode());
    }

    @Test
    void atendimentoInexistente_devolve404() {
        when(guiaProcRepository.findById(10L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.criarEspelhoComItens(pedido(10L)));
        assertEquals(ErrorCode.ATENDIMENTO_NAO_ENCONTRADO, ex.getErrorCode());
    }

    @Test
    void atendimentoRepetidoNoPedido_devolve400() {
        when(guiaProcRepository.findById(10L)).thenReturn(Optional.of(atendimento(10L, OCS, "REALIZADA", "1", "X")));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.criarEspelhoComItens(pedido(10L, 10L)));
        assertEquals(ErrorCode.DADOS_INVALIDOS, ex.getErrorCode());
    }

    @Test
    void atendimentoJaEmOutroEspelho_devolve409() {
        when(guiaProcRepository.findById(10L)).thenReturn(Optional.of(atendimento(10L, OCS, "REALIZADA", "1", "X")));
        EspelhoItemModel existente = new EspelhoItemModel();
        existente.setEspelhoId(3);
        existente.setAtendimentoId(10L);
        EspelhoModel outro = new EspelhoModel();
        outro.setId(3);
        outro.setStatus(StatusEspelho.APROVADO);
        when(espelhoItemRepository.findByAtendimentoIdIn(anyList())).thenReturn(List.of(existente));
        when(espelhoRepository.findById(3)).thenReturn(Optional.of(outro));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.criarEspelhoComItens(pedido(10L)));
        assertEquals(ErrorCode.ATENDIMENTO_JA_EM_ESPELHO, ex.getErrorCode());
        verify(espelhoRepository, never()).save(any());
    }

    @Test
    void atendimentoDeEspelhoCancelado_podeEntrarDeNovo() {
        when(guiaProcRepository.findById(10L)).thenReturn(Optional.of(atendimento(10L, OCS, "REALIZADA", "1", "X")));
        EspelhoItemModel existente = new EspelhoItemModel();
        existente.setEspelhoId(3);
        existente.setAtendimentoId(10L);
        EspelhoModel cancelado = new EspelhoModel();
        cancelado.setId(3);
        cancelado.setStatus(StatusEspelho.CANCELADO);
        when(espelhoItemRepository.findByAtendimentoIdIn(anyList())).thenReturn(List.of(existente));
        when(espelhoRepository.findById(3)).thenReturn(Optional.of(cancelado));
        salvarDevolvendoId();

        assertDoesNotThrow(() -> service.criarEspelhoComItens(pedido(10L)));
        verify(espelhoItemRepository).save(any(EspelhoItemModel.class));
    }

    @Test
    void periodoInvalido_naoBuscaAtendimentos() {
        doThrow(new BusinessException(ErrorCode.FORA_DO_MES))
                .when(validarPeriodoDoEspelhoService).validar(any(), any(), any());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.criarEspelhoComItens(pedido(10L)));
        assertEquals(ErrorCode.FORA_DO_MES, ex.getErrorCode());
        verifyNoInteractions(guiaProcRepository, espelhoRepository);
    }
}
