package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.atendimento.sessao.SessaoModel;
import com.datasphere.fusex.atendimento.sessao.SessaoRepository;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.dto.request.SessaoRequest;
import com.datasphere.fusex.dto.request.SessaoResponse;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarSessaoServiceTest {

    @Mock
    private GuiaProcRepository guiaProcRepository;

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private ValidarSessaoService validarSessaoService;

    @InjectMocks
    private RegistrarSessaoService registrarSessaoService;

    // ============================================================
    // Cenário feliz: 1ª sessão → vira EM_ANDAMENTO
    // ============================================================
    @Test
    @DisplayName("Deve registrar 1ª sessão e mudar procedimento para EM_ANDAMENTO")
    void deveRegistrarPrimeiraSessao() {
        Long atendimentoId = 10L;
        GuiaProcModel atendimento = atendimentoValido();
        atendimento.setQtdAutorizada(10);
        atendimento.setQtdRealizada(0);
        atendimento.setStatus("EMITIDA");

        when(guiaProcRepository.findById(atendimentoId)).thenReturn(Optional.of(atendimento));
        when(sessaoRepository.findMaxNumeroSessaoByGuiaProcId(atendimentoId))
                .thenReturn(Optional.empty());
        when(sessaoRepository.existsByGuiaProc_GuiaId(atendimentoId)).thenReturn(false);
        when(sessaoRepository.save(any(SessaoModel.class))).thenAnswer(i -> i.getArgument(0));

        SessaoResponse response = registrarSessaoService.registrarSessao(
                atendimentoId, new SessaoRequest("12345678000199"));

        assertThat(response.atendimentoId()).isEqualTo(10L);
        assertThat(response.numeroSessao()).isEqualTo(1);
        assertThat(response.qtdAutorizada()).isEqualTo(10);
        assertThat(response.qtdRealizada()).isEqualTo(1);
        assertThat(response.saldo()).isEqualTo(9);
        assertThat(response.statusProcedimento()).isEqualTo("EM_ANDAMENTO");
        assertThat(response.dataRealizacao()).isNotNull();

        assertThat(atendimento.getStatus()).isEqualTo("EM_ANDAMENTO");
        assertThat(atendimento.getQtdRealizada()).isEqualTo(1);

        verify(validarSessaoService).validarOcsDoContrato(atendimento, "12345678000199");
        verify(validarSessaoService).validarGuiaEmitida(atendimento);
        verify(validarSessaoService).validarGuiaDentroDaValidade(atendimento);
        verify(validarSessaoService).validarSaldoDeSessoes(atendimento);
        verify(sessaoRepository).save(any(SessaoModel.class));
        verify(guiaProcRepository).save(atendimento);
    }

    // ============================================================
    // Cenário: 3ª sessão (numeração sequencial sem buracos)
    // ============================================================
    @Test
    @DisplayName("Deve registrar 3ª sessão mantendo numeração sequencial")
    void deveRegistrarTerceiraSessao() {
        Long atendimentoId = 10L;
        GuiaProcModel atendimento = atendimentoValido();
        atendimento.setQtdAutorizada(10);
        atendimento.setQtdRealizada(2);
        atendimento.setStatus("EM_ANDAMENTO");

        when(guiaProcRepository.findById(atendimentoId)).thenReturn(Optional.of(atendimento));
        when(sessaoRepository.findMaxNumeroSessaoByGuiaProcId(atendimentoId))
                .thenReturn(Optional.of(2));
        when(sessaoRepository.existsByGuiaProc_GuiaId(atendimentoId)).thenReturn(true);
        when(sessaoRepository.save(any(SessaoModel.class))).thenAnswer(i -> i.getArgument(0));

        SessaoResponse response = registrarSessaoService.registrarSessao(
                atendimentoId, new SessaoRequest("12345678000199"));

        assertThat(response.numeroSessao()).isEqualTo(3);
        assertThat(response.qtdRealizada()).isEqualTo(3);
        assertThat(response.saldo()).isEqualTo(7);
        assertThat(atendimento.getStatus()).isEqualTo("EM_ANDAMENTO");

        ArgumentCaptor<SessaoModel> captor = ArgumentCaptor.forClass(SessaoModel.class);
        verify(sessaoRepository).save(captor.capture());
        assertThat(captor.getValue().getNumeroSessao()).isEqualTo(3);
    }

    // ============================================================
    // Cenário: erro — atendimento não encontrado
    // ============================================================
    @Test
    @DisplayName("Deve lançar ATENDIMENTO_NAO_ENCONTRADO quando id não existe")
    void deveLancarErroQuandoAtendimentoNaoExiste() {
        when(guiaProcRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrarSessaoService.registrarSessao(
                999L, new SessaoRequest("12345678000199")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ATENDIMENTO_NAO_ENCONTRADO);

        verifyNoInteractions(sessaoRepository);
        verify(guiaProcRepository, never()).save(any());
    }

    // ============================================================
    // Cenário: erro — sessões esgotadas
    // ============================================================
    @Test
    @DisplayName("Deve lançar SESSOES_ESGOTADAS quando qtdRealizada >= qtdAutorizada")
    void deveLancarErroQuandoSemSaldo() {
        Long atendimentoId = 10L;
        GuiaProcModel atendimento = atendimentoValido();
        atendimento.setQtdAutorizada(3);
        atendimento.setQtdRealizada(3);

        when(guiaProcRepository.findById(atendimentoId)).thenReturn(Optional.of(atendimento));
        doThrow(new BusinessException(ErrorCode.SESSOES_ESGOTADAS))
                .when(validarSessaoService).validarSaldoDeSessoes(atendimento);

        assertThatThrownBy(() -> registrarSessaoService.registrarSessao(
                atendimentoId, new SessaoRequest("12345678000199")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SESSOES_ESGOTADAS);

        verify(sessaoRepository, never()).save(any());
        verify(guiaProcRepository, never()).save(any());
    }

    // ============================================================
    // Helper
    // ============================================================
    private GuiaProcModel atendimentoValido() {
        GuiaModel guia = new GuiaModel();
        guia.setStatus("EMITIDA");

        GuiaProcModel atendimento = new GuiaProcModel();
        atendimento.setGuiaId(10L);
        atendimento.setGuiaModel(guia);
        atendimento.setStatus("EMITIDA");
        atendimento.setDataValidade(LocalDate.now().plusDays(30));
        return atendimento;
    }
}