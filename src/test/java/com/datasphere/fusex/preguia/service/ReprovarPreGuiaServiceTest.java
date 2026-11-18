package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.StatusPreGuia;
import com.datasphere.fusex.preguia.service.IdentificarAprovadorService.Aprovador;
import com.datasphere.fusex.preguia.service.IdentificarAprovadorService.Papel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @InjectMocks
    private ReprovarPreGuiaService service;

    private PreGuiaModel preGuiaValidada() {
        PreGuiaModel p = new PreGuiaModel();
        p.setId(7L);
        p.setStatus(StatusPreGuia.VALIDADA);
        return p;
    }

    @Test
    void semMotivo_lancaMotivoObrigatorio() {
        PreGuiaModel p = preGuiaValidada();
        when(buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(7L)).thenReturn(p);
        when(identificarAprovadorService.identificar(p)).thenReturn(new Aprovador(Papel.CHEFE, 1, 1, "Teste"));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.reprovar(7L, "  "));

        assertEquals(ErrorCode.MOTIVO_OBRIGATORIO, ex.getErrorCode());
        verifyNoInteractions(registrarStatusPreGuiaService);
    }

    @Test
    void comMotivo_reprovaEGuardaOMotivo() {
        PreGuiaModel p = preGuiaValidada();
        when(buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(7L)).thenReturn(p);
        when(identificarAprovadorService.identificar(p)).thenReturn(new Aprovador(Papel.MEDICO, 1, 1, "Teste"));

        service.reprovar(7L, " Procedimento sem justificativa ");

        assertEquals("Procedimento sem justificativa", p.getMotivoReprovacao());
        verify(registrarStatusPreGuiaService).atualizarStatus(p, StatusPreGuia.REPROVADA, "reprovacao-pre-guia");
    }
}
