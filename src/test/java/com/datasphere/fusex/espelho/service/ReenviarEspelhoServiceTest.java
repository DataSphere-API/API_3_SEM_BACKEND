package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.DecisaoEspelhoResponse;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.service.RegistrarMudancaStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReenviarEspelhoServiceTest {

    @Mock
    private EspelhoRepository espelhoRepository;

    @Mock
    private RegistrarMudancaStatusService registrarMudancaStatusService;

    @InjectMocks
    private ReenviarEspelhoService service;

    private EspelhoModel espelho;

    @BeforeEach
    void setUp() {
        espelho = new EspelhoModel();
        espelho.setId(1);
        espelho.setStatus(StatusEspelho.REPROVADO);
        espelho.setMotivoReprovacao("Valor incorreto");
        espelho.setAuditorId(2);
    }

    @Test
    void reenviarEspelhoReprovado_voltaParaEmAbertoLimpaMotivoAuditorEGravaHistorico() {
        when(espelhoRepository.findById(1)).thenReturn(Optional.of(espelho));

        DecisaoEspelhoResponse resposta = service.reenviarEspelho(1);

        assertEquals(1, resposta.id());
        assertEquals(StatusEspelho.EM_ABERTO, resposta.status());
        assertNull(resposta.motivo());
        assertNull(resposta.auditorId());

        assertEquals(StatusEspelho.EM_ABERTO, espelho.getStatus());
        assertNull(espelho.getMotivoReprovacao());
        assertNull(espelho.getAuditorId());
        verify(espelhoRepository).save(espelho);
        verify(registrarMudancaStatusService).executar(
                EntidadeTipo.ESPELHO,
                1L,
                "REPROVADO",
                "EM_ABERTO",
                "reenvio-espelho"
        );
    }

    @Test
    void reenviarEspelhoNaoReprovado_lanca409() {
        espelho.setStatus(StatusEspelho.EM_ABERTO);
        when(espelhoRepository.findById(1)).thenReturn(Optional.of(espelho));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.reenviarEspelho(1));
        assertEquals(ErrorCode.ESPELHO_NAO_REPROVADO, ex.getErrorCode());
        verify(espelhoRepository, never()).save(any());
        verifyNoInteractions(registrarMudancaStatusService);
    }

    @Test
    void reenviarEspelhoAprovado_lanca409() {
        espelho.setStatus(StatusEspelho.APROVADO);
        when(espelhoRepository.findById(1)).thenReturn(Optional.of(espelho));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.reenviarEspelho(1));
        assertEquals(ErrorCode.ESPELHO_NAO_REPROVADO, ex.getErrorCode());
        verify(espelhoRepository, never()).save(any());
        verifyNoInteractions(registrarMudancaStatusService);
    }

    @Test
    void reenviarEspelhoInexistente_lanca404() {
        when(espelhoRepository.findById(99)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.reenviarEspelho(99));
        assertEquals(ErrorCode.ESPELHO_NAO_ENCONTRADO, ex.getErrorCode());
        verify(espelhoRepository, never()).save(any());
        verifyNoInteractions(registrarMudancaStatusService);
    }
}
