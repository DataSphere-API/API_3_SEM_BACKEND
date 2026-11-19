package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelho.dto.DecisaoEspelhoResponse;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.FaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.fatura.service.AlimentarFaturaNaAprovacaoService;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.service.RegistrarMudancaStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AprovarEspelhoServiceTest {

    @Mock
    private EspelhoRepository espelhoRepository;

    @Mock
    private RegistrarMudancaStatusService registrarMudancaStatusService;

    @Mock
    private FaturaRepository faturaRepository;

    @Mock
    private AlimentarFaturaNaAprovacaoService alimentarFaturaNaAprovacaoService;

    @InjectMocks
    private AprovarEspelhoService service;

    private EspelhoModel espelho;

    @BeforeEach
    void setUp() {
        espelho = new EspelhoModel();
        espelho.setId(1);
        espelho.setStatus(StatusEspelho.EM_ABERTO);
        espelho.setOcsId("12345678000199");
        espelho.setDataInicio(LocalDate.of(2026, 11, 1));
        espelho.setDataFim(LocalDate.of(2026, 11, 30));
    }

    @Test
    void aprovarEspelhoEmAberto_mudaStatusGravaAuditorEHistorico() {
        when(espelhoRepository.findById(1)).thenReturn(Optional.of(espelho));
        when(faturaRepository.findByOcsIdAndCompetenciaAndStatusIn(eq("12345678000199"), eq("2026-11"), anyList()))
                .thenReturn(List.of());

        DecisaoEspelhoResponse resposta = service.aprovarEspelho(1, 2);

        assertEquals(1, resposta.id());
        assertEquals(StatusEspelho.APROVADO, resposta.status());
        verify(alimentarFaturaNaAprovacaoService).processarAprovacaoEspelho(espelho);
        assertNull(resposta.motivo());
        assertEquals(2, resposta.auditorId());

        assertEquals(StatusEspelho.APROVADO, espelho.getStatus());
        assertEquals(2, espelho.getAuditorId());
        verify(espelhoRepository).save(espelho);
        verify(registrarMudancaStatusService).executar(
                EntidadeTipo.ESPELHO,
                1L,
                "EM_ABERTO",
                "APROVADO",
                "auditoria-aprovacao"
        );
    }

    @Test
    void aprovarEspelhoInexistente_lanca404() {
        when(espelhoRepository.findById(99)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.aprovarEspelho(99, 2));
        assertEquals(ErrorCode.ESPELHO_NAO_ENCONTRADO, ex.getErrorCode());
        verify(espelhoRepository, never()).save(any());
        verifyNoInteractions(registrarMudancaStatusService);
    }

    @Test
    void aprovarEspelhoJaAuditado_lanca409() {
        espelho.setStatus(StatusEspelho.APROVADO);
        when(espelhoRepository.findById(1)).thenReturn(Optional.of(espelho));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.aprovarEspelho(1, 2));
        assertEquals(ErrorCode.ESPELHO_JA_AUDITADO, ex.getErrorCode());
        verify(espelhoRepository, never()).save(any());
        verifyNoInteractions(registrarMudancaStatusService);
    }

    @Test
    void aprovarEspelhoComFaturaJaEncaminhada_lanca409() {
        when(espelhoRepository.findById(1)).thenReturn(Optional.of(espelho));
        FaturaModel fatura = new FaturaModel();
        fatura.setStatus(StatusFatura.ENCAMINHADA_APROVACAO);
        when(faturaRepository.findByOcsIdAndCompetenciaAndStatusIn(eq("12345678000199"), eq("2026-11"), anyList()))
                .thenReturn(List.of(fatura));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.aprovarEspelho(1, 2));
        assertEquals(ErrorCode.FATURA_JA_ENCAMINHADA, ex.getErrorCode());
        verify(espelhoRepository, never()).save(any());
        verifyNoInteractions(registrarMudancaStatusService, alimentarFaturaNaAprovacaoService);
    }

    @Test
    void aprovarEspelhoComFaturaJaLiquidada_tambemLanca409() {
        when(espelhoRepository.findById(1)).thenReturn(Optional.of(espelho));
        FaturaModel fatura = new FaturaModel();
        fatura.setStatus(StatusFatura.LIQUIDADA);
        when(faturaRepository.findByOcsIdAndCompetenciaAndStatusIn(eq("12345678000199"), eq("2026-11"),
                argThat(lista -> lista.contains(StatusFatura.LIQUIDADA) && !lista.contains(StatusFatura.ATIVA))))
                .thenReturn(List.of(fatura));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.aprovarEspelho(1, 2));
        assertEquals(ErrorCode.FATURA_JA_ENCAMINHADA, ex.getErrorCode());
        verifyNoInteractions(alimentarFaturaNaAprovacaoService);
    }
}
