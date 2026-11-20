package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.StatusPreGuia;
import com.datasphere.fusex.preguia.dto.AprovacaoResponse;
import com.datasphere.fusex.preguia.service.IdentificarAprovadorService.Aprovador;
import com.datasphere.fusex.preguia.service.IdentificarAprovadorService.Papel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReprovarPreGuiaServiceTest {

    @Mock
    private BuscarPreGuiaPorIdService buscarPreGuiaPorIdService;
    @Mock
    private IdentificarAprovadorService identificarAprovadorService;
    @Mock
    private RegistrarStatusPreGuiaService registrarStatusPreGuiaService;

    private ReprovarPreGuiaService service;

    @BeforeEach
    void preparar() {
        service = new ReprovarPreGuiaService(
                buscarPreGuiaPorIdService,
                identificarAprovadorService,
                new ValidarPreGuiaAguardandoDecisaoService(),
                new AssinarDecisaoDaPreGuiaService(),
                registrarStatusPreGuiaService,
                new MontarAssinaturaService());
    }

    private PreGuiaModel preGuia(StatusPreGuia status) {
        PreGuiaModel p = new PreGuiaModel();
        p.setId(7L);
        p.setStatus(status);
        when(buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(7L)).thenReturn(p);
        return p;
    }

    @Test
    void semMotivo_lancaMotivoObrigatorio() {
        PreGuiaModel p = preGuia(StatusPreGuia.VALIDADA);
        when(identificarAprovadorService.identificar(p)).thenReturn(new Aprovador(Papel.CHEFE, 3, 1, "Roberto Lima"));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.reprovar(7L, "  "));

        assertEquals(ErrorCode.MOTIVO_OBRIGATORIO, ex.getErrorCode());
        verifyNoInteractions(registrarStatusPreGuiaService);
    }

    @Test
    void comMotivo_reprovaGuardaOMotivoEAssina() {
        PreGuiaModel p = preGuia(StatusPreGuia.VALIDADA);
        when(identificarAprovadorService.identificar(p)).thenReturn(new Aprovador(Papel.MEDICO, 1, 2, "Carlos Medeiros"));

        AprovacaoResponse resposta = service.reprovar(7L, " Procedimento sem justificativa ");

        assertEquals("Procedimento sem justificativa", p.getMotivoReprovacao());
        assertEquals("MEDICO", resposta.assinatura().perfil());
        assertEquals(p.getDataReprovacao(), resposta.assinatura().dataHora());
        verify(registrarStatusPreGuiaService).atualizarStatus(p, StatusPreGuia.REPROVADA, "reprovacao-pre-guia");
    }

    @Test
    void preGuiaJaAprovada_naoPodeSerReprovada() {
        PreGuiaModel p = preGuia(StatusPreGuia.APROVADA);
        when(identificarAprovadorService.identificar(p)).thenReturn(new Aprovador(Papel.CHEFE, 3, 1, "Roberto Lima"));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.reprovar(7L, "Fora do prazo"));

        assertEquals(ErrorCode.APROVACAO_JA_REGISTRADA, ex.getErrorCode());
        assertNull(p.getMotivoReprovacao());
        verifyNoInteractions(registrarStatusPreGuiaService);
    }
}
