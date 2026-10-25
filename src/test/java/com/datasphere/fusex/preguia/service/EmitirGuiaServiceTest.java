package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.service.ValidadeGuiaService;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import com.datasphere.fusex.preguia.PreGuiaProcRepository;
import com.datasphere.fusex.preguia.StatusPreGuia;
import com.datasphere.fusex.preguia.dto.EmissaoResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmitirGuiaServiceTest {

    @Mock
    private BuscarPreGuiaPorIdService buscarPreGuiaPorIdService;
    @Mock
    private PreGuiaProcRepository preGuiaProcRepository;
    @Mock
    private CriarGuiaDaPreGuiaService criarGuiaDaPreGuiaService;
    @Mock
    private CriarProcedimentosDaGuiaService criarProcedimentosDaGuiaService;
    @Mock
    private RegistrarStatusPreGuiaService registrarStatusPreGuiaService;
    @Mock
    private ValidadeGuiaService validadeGuiaService;

    @InjectMocks
    private EmitirGuiaService service;

    private PreGuiaModel preGuia(StatusPreGuia status) {
        PreGuiaModel p = new PreGuiaModel();
        p.setId(7L);
        p.setStatus(status);
        return p;
    }

    @Test
    void preGuiaJaEmitida_lancaErro() {
        when(buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(7L)).thenReturn(preGuia(StatusPreGuia.EMITIDA));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.emitir(7L));

        assertEquals(ErrorCode.PRE_GUIA_JA_EMITIDA, ex.getErrorCode());
        verifyNoInteractions(criarGuiaDaPreGuiaService, criarProcedimentosDaGuiaService);
    }

    @Test
    void preGuiaSemAsDuasAprovacoes_lancaErro() {
        when(buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(7L)).thenReturn(preGuia(StatusPreGuia.VALIDADA));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.emitir(7L));

        assertEquals(ErrorCode.PRE_GUIA_NAO_APROVADA, ex.getErrorCode());
        verifyNoInteractions(criarGuiaDaPreGuiaService, criarProcedimentosDaGuiaService);
    }

    @Test
    void preGuiaAprovada_emiteGuiaComValidadeDe60Dias() {
        PreGuiaModel p = preGuia(StatusPreGuia.APROVADA);
        GuiaModel guia = new GuiaModel();
        guia.setId(4L);
        guia.setStatus("EMITIDA");
        GuiaProcModel procedimento = new GuiaProcModel();
        procedimento.setGuiaId(10L);
        LocalDate validoAte = LocalDate.now().plusDays(60);

        when(buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(7L)).thenReturn(p);
        when(preGuiaProcRepository.findByPreGuiaModel_IdOrderByIdAsc(7L)).thenReturn(List.<PreGuiaProcModel>of());
        when(criarGuiaDaPreGuiaService.criarGuia(any(PreGuiaModel.class), any(LocalDate.class))).thenReturn(guia);
        when(criarProcedimentosDaGuiaService.criarProcedimentos(any(GuiaModel.class), any(List.class), any(LocalDate.class)))
                .thenReturn(List.of(procedimento));
        when(validadeGuiaService.calcularDataValidade(any(LocalDate.class))).thenReturn(validoAte);

        EmissaoResponse resposta = service.emitir(7L);

        assertEquals(4L, resposta.guiaId());
        assertEquals("EMITIDA", resposta.status());
        assertEquals(LocalDate.now(), resposta.dataEmissao());
        assertEquals(validoAte, resposta.validoAte());
        assertEquals(10L, resposta.procedimentos().get(0).atendimentoId());
        assertEquals(4L, p.getGuiaId());
        verify(registrarStatusPreGuiaService).atualizarStatus(p, StatusPreGuia.EMITIDA, "emissao-da-guia");
    }
}