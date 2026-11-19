package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.EspelhoRequest;
import com.datasphere.fusex.espelho.dto.EspelhoResponse;
import com.datasphere.fusex.espelho.dto.ItemEspelhoRequest;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.service.RegistrarMudancaStatusService;
import com.datasphere.fusex.ocs.OcsModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CriarEspelhoComItensServiceTest {

    @Mock private EspelhoRepository espelhoRepository;
    @Mock private EspelhoItemRepository espelhoItemRepository;
    @Mock private VincularItemAoAtendimentoService vincularItemAoAtendimentoService;
    @Mock private VincularItemAoBeneficiarioService vincularItemAoBeneficiarioService;
    @Mock private ValidarAtendimentoRealizadoService validarAtendimentoRealizadoService;
    @Mock private ValidarEspelhoNaoDuplicadoPorAtendimentoService validarEspelhoNaoDuplicadoPorAtendimentoService;
    @Mock private ValidarOcsDoAtendimentoService validarOcsDoAtendimentoService;
    @Mock private ValidarBeneficiarioDaGuiaService validarBeneficiarioDaGuiaService;
    @Mock private RegistrarMudancaStatusService registrarMudancaStatusService;

    @InjectMocks
    private CriarEspelhoComItensService service;

    private GuiaProcModel atendimento;

    @BeforeEach
    void setUp() {
        atendimento = new GuiaProcModel();
        atendimento.setId(10L);
        ContratoModel contrato = new ContratoModel();
        contrato.setOcsModel(new OcsModel("12345678000199", "Clinica Teste"));
        atendimento.setContratoModel(contrato);
    }

    @Test
    void criarEspelhoSemItens_devolve422() {
        EspelhoRequest request = new EspelhoRequest(10L, "12345678000199", List.of());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.criarEspelhoComItens(request));
        assertEquals(ErrorCode.ESPELHO_SEM_ITENS, ex.getErrorCode());
        verifyNoInteractions(espelhoRepository, espelhoItemRepository);
    }

    @Test
    void atendimentoOutraOcs_recusadoCom422ENadaGravado() {
        ItemEspelhoRequest item = new ItemEspelhoRequest("123456789-00", "Fisioterapia", new BigDecimal("100.00"));
        EspelhoRequest request = new EspelhoRequest(10L, "99999999000199", List.of(item));

        when(validarAtendimentoRealizadoService.validarAtendimentoRealizado(10L)).thenReturn(atendimento);
        doThrow(new BusinessException(ErrorCode.OCS_DIVERGENTE, List.of("ocsId: divergente")))
                .when(validarOcsDoAtendimentoService).validarOcs("99999999000199", atendimento);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.criarEspelhoComItens(request));
        assertEquals(ErrorCode.OCS_DIVERGENTE, ex.getErrorCode());
        verifyNoInteractions(espelhoRepository, espelhoItemRepository);
    }

    @Test
    void falhaNoSegundoItem_recusaCom422ENaoGravaEspelho() {
        ItemEspelhoRequest item1 = new ItemEspelhoRequest("123456789-00", "Fisio 1", new BigDecimal("100.00"));
        ItemEspelhoRequest item2 = new ItemEspelhoRequest("987654321-00", "Fisio 2", new BigDecimal("100.00"));
        EspelhoRequest request = new EspelhoRequest(10L, "12345678000199", List.of(item1, item2));

        when(validarAtendimentoRealizadoService.validarAtendimentoRealizado(10L)).thenReturn(atendimento);
        when(validarEspelhoNaoDuplicadoPorAtendimentoService.validarEspelhoNaoDuplicadoPorAtendimento(10L)).thenReturn(true);
        doThrow(new BusinessException(ErrorCode.BENEFICIARIO_FORA_DA_GUIA, List.of("itens[1].beneficiarioId: erro")))
                .when(validarBeneficiarioDaGuiaService).validarBeneficiarios(10L, request.itens());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.criarEspelhoComItens(request));
        assertEquals(ErrorCode.BENEFICIARIO_FORA_DA_GUIA, ex.getErrorCode());

        verify(espelhoRepository, never()).save(any());
        verify(espelhoItemRepository, never()).save(any());
        verifyNoInteractions(registrarMudancaStatusService);
    }

    @Test
    void criarEspelhoComSucesso_gravaEspelhoItensEHistorico() {
        ItemEspelhoRequest itemRequest = new ItemEspelhoRequest("123456789-00", "Consulta", new BigDecimal("150.00"));
        EspelhoRequest request = new EspelhoRequest(10L, "12345678000199", List.of(itemRequest));

        when(validarAtendimentoRealizadoService.validarAtendimentoRealizado(10L)).thenReturn(atendimento);
        when(validarEspelhoNaoDuplicadoPorAtendimentoService.validarEspelhoNaoDuplicadoPorAtendimento(10L)).thenReturn(true);

        EspelhoModel espelhoSalvo = new EspelhoModel();
        espelhoSalvo.setId(5);
        espelhoSalvo.setAtendimentoId(10L);
        espelhoSalvo.setStatus(StatusEspelho.EM_ABERTO);
        when(espelhoRepository.save(any(EspelhoModel.class))).thenReturn(espelhoSalvo);

        EspelhoItemModel itemSalvo = new EspelhoItemModel();
        itemSalvo.setId(1);
        itemSalvo.setEspelhoId(5);
        itemSalvo.setDescricao("Consulta");
        itemSalvo.setValor(new BigDecimal("150.00"));
        itemSalvo.setBeneficiarioId("123456789-00");
        when(espelhoItemRepository.save(any(EspelhoItemModel.class))).thenReturn(itemSalvo);

        EspelhoResponse resposta = service.criarEspelhoComItens(request);

        assertNotNull(resposta);
        verify(espelhoRepository).save(any(EspelhoModel.class));
        verify(espelhoItemRepository).save(any(EspelhoItemModel.class));
        verify(registrarMudancaStatusService).executar(
                EntidadeTipo.ESPELHO,
                5L,
                null,
                StatusEspelho.EM_ABERTO.name(),
                "criacao-espelho"
        );
    }
}
