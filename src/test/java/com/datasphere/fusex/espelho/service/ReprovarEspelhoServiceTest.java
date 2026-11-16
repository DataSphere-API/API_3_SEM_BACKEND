package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.request.DecisaoEspelhoResponse;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
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
class ReprovarEspelhoServiceTest {

    @Mock
    private EspelhoRepository espelhoRepository;

    @Mock
    private RegistrarMudancaStatusService registrarMudancaStatusService;

    @InjectMocks
    private ReprovarEspelhoService service;

    private EspelhoModel espelho;

    @BeforeEach
    void setUp() {
        espelho = new EspelhoModel();
        espelho.setId(1);
        espelho.setStatus(StatusEspelho.EM_ABERTO);
    }

    @Test
    void reprovarEspelhoEmAbertoComMotivo_mudaStatusGravaMotivoAuditorEHistorico() {
        when(espelhoRepository.findById(1)).thenReturn(Optional.of(espelho));

        DecisaoEspelhoResponse resposta = service.reprovarEspelho(1, 2, "Valor acima do contrato");

        assertEquals(1, resposta.id());
        assertEquals(StatusEspelho.REPROVADO, resposta.status());
        assertEquals("Valor acima do contrato", resposta.motivo());
        assertEquals(2, resposta.auditorId());

        assertEquals(StatusEspelho.REPROVADO, espelho.getStatus());
        assertEquals("Valor acima do contrato", espelho.getMotivoReprovacao());
        assertEquals(2, espelho.getAuditorId());
        verify(espelhoRepository).save(espelho);
        verify(registrarMudancaStatusService).executar(
                EntidadeTipo.ESPELHO,
                1L,
                "EM_ABERTO",
                "REPROVADO",
                "auditoria-reprovacao"
        );
    }

    @Test
    void reprovarEspelhoSemMotivo_lanca400() {
        when(espelhoRepository.findById(1)).thenReturn(Optional.of(espelho));

        BusinessException ex1 = assertThrows(BusinessException.class, () -> service.reprovarEspelho(1, 2, null));
        assertEquals(ErrorCode.MOTIVO_OBRIGATORIO, ex1.getErrorCode());

        BusinessException ex2 = assertThrows(BusinessException.class, () -> service.reprovarEspelho(1, 2, "   "));
        assertEquals(ErrorCode.MOTIVO_OBRIGATORIO, ex2.getErrorCode());

        verify(espelhoRepository, never()).save(any());
        verifyNoInteractions(registrarMudancaStatusService);
    }

    @Test
    void reprovarEspelhoJaAuditado_lanca409() {
        espelho.setStatus(StatusEspelho.REPROVADO);
        when(espelhoRepository.findById(1)).thenReturn(Optional.of(espelho));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.reprovarEspelho(1, 2, "Novo motivo"));
        assertEquals(ErrorCode.ESPELHO_JA_AUDITADO, ex.getErrorCode());
        verify(espelhoRepository, never()).save(any());
        verifyNoInteractions(registrarMudancaStatusService);
    }

    @Test
    void reprovarEspelhoInexistente_lanca404() {
        when(espelhoRepository.findById(99)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.reprovarEspelho(99, 2, "Motivo qualquer"));
        assertEquals(ErrorCode.ESPELHO_NAO_ENCONTRADO, ex.getErrorCode());
        verify(espelhoRepository, never()).save(any());
        verifyNoInteractions(registrarMudancaStatusService);
    }
}
