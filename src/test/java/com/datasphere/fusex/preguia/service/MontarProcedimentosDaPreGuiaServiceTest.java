package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.ocs.OcsModel;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import com.datasphere.fusex.preguia.dto.PreGuiaProcedimentoResponse;
import com.datasphere.fusex.procedimento.ProcedimentoModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MontarProcedimentosDaPreGuiaServiceTest {

    @Mock
    private CalcularValorTotalPreGuiaService calcularValorTotalPreGuiaService;

    @InjectMocks
    private MontarProcedimentosDaPreGuiaService service;

    private PreGuiaProcModel procedimento(Long id, Long guiaProcId) {
        PreGuiaProcModel p = new PreGuiaProcModel();
        p.setId(id);
        p.setGuiaProcId(guiaProcId);
        p.setProcedimentoModel(new ProcedimentoModel(1, "Sessão de psicoterapia"));
        p.setOcsModel(new OcsModel("12345678000199", "Clínica X"));
        p.setQtdAutorizada(2);
        return p;
    }

    @Test
    void atendimentoId_eOIdDoGuiaProc_eNaoODoPreGuiaProc() {
        PreGuiaProcModel p = procedimento(5L, 42L);
        when(calcularValorTotalPreGuiaService.calcularValorUnitario(p)).thenReturn(new BigDecimal("150.00"));
        when(calcularValorTotalPreGuiaService.calcularValorDoProcedimento(p)).thenReturn(new BigDecimal("300.00"));

        List<PreGuiaProcedimentoResponse> respostas = service.montar(List.of(p));

        assertEquals(42L, respostas.get(0).atendimentoId());
    }

    @Test
    void antesDaEmissao_atendimentoIdEhNulo() {
        PreGuiaProcModel p = procedimento(5L, null);
        when(calcularValorTotalPreGuiaService.calcularValorUnitario(p)).thenReturn(new BigDecimal("150.00"));
        when(calcularValorTotalPreGuiaService.calcularValorDoProcedimento(p)).thenReturn(new BigDecimal("300.00"));

        List<PreGuiaProcedimentoResponse> respostas = service.montar(List.of(p));

        assertNull(respostas.get(0).atendimentoId());
    }
}