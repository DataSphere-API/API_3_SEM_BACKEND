package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import com.datasphere.fusex.medico.MedicoRepository;
import com.datasphere.fusex.ocs.OcsModel;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import com.datasphere.fusex.preguia.PreGuiaProcRepository;
import com.datasphere.fusex.preguia.dto.PendenciaResponse;
import com.datasphere.fusex.procedimento.ProcedimentoModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreGuiaValidacaoServiceTest {

    @Mock
    private BeneficiarioRepository beneficiarioRepository;
    @Mock
    private MedicoRepository medicoRepository;
    @Mock
    private PreGuiaProcRepository preGuiaProcRepository;
    @Mock
    private BuscarContratoDaPreGuiaProcService buscarContratoDaPreGuiaProcService;

    @InjectMocks
    private PreGuiaValidacaoService service;

    private PreGuiaProcModel procedimento(Long id, int quantidade) {
        PreGuiaProcModel p = new PreGuiaProcModel();
        p.setId(id);
        p.setProcedimentoModel(new ProcedimentoModel(1, "Sessão de psicoterapia"));
        p.setOcsModel(new OcsModel("12345678000199", "Clínica X"));
        p.setQtdAutorizada(quantidade);
        return p;
    }

    @Test
    void quantidadeZero_geraPendenciaDeSessoesInvalidas() {
        List<PendenciaResponse> pendencias = service.validarQuantidadeDeSessoes(List.of(procedimento(12L, 0)));

        assertEquals(1, pendencias.size());
        assertEquals(PendenciaResponse.CodigoPendencia.SESSOES_INVALIDAS, pendencias.get(0).codigo());
        assertEquals(12L, pendencias.get(0).atendimentoId());
    }

    @Test
    void quantidadeValida_naoGeraPendencia() {
        List<PendenciaResponse> pendencias = service.validarQuantidadeDeSessoes(List.of(procedimento(12L, 3)));

        assertTrue(pendencias.isEmpty());
    }

    @Test
    void semProcedimentos_geraPendenciaDePreGuiaVazia() {
        List<PendenciaResponse> pendencias = service.validarProcedimentosDaPreGuia(List.of());

        assertEquals(1, pendencias.size());
        assertEquals(PendenciaResponse.CodigoPendencia.PRE_GUIA_SEM_PROCEDIMENTOS, pendencias.get(0).codigo());
    }

    @Test
    void procedimentoSemContrato_geraPendenciaDeContrato() {
        PreGuiaProcModel p = procedimento(13L, 1);
        when(buscarContratoDaPreGuiaProcService.buscarContrato(p)).thenReturn(null);

        List<PendenciaResponse> pendencias = service.validarContratoDosProcedimentos(List.of(p));

        assertEquals(1, pendencias.size());
        assertEquals(PendenciaResponse.CodigoPendencia.PROCEDIMENTO_SEM_CONTRATO, pendencias.get(0).codigo());
        assertEquals(13L, pendencias.get(0).atendimentoId());
    }
}