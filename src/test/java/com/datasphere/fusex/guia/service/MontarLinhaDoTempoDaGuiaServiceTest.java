package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.HistoricoStatusModel;
import com.datasphere.fusex.historicostatus.service.ListarHistoricoPorEntidadeService;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MontarLinhaDoTempoDaGuiaServiceTest {

    @Mock private GuiaRepository guiaRepository;
    @Mock private PreGuiaRepository preGuiaRepository;
    @Mock private GuiaProcRepository guiaProcRepository;
    @Mock private ListarHistoricoPorEntidadeService historico;
    @Mock private BuscarUsuarioLogadoService usuarioLogado;
    @InjectMocks private MontarLinhaDoTempoDaGuiaService service;

    @BeforeEach
    void autenticar() {
        when(usuarioLogado.buscarUsuarioLogado()).thenReturn(usuario("123456789-00", Perfil.BENEFICIARIO));
    }

    @Test
    void reunePreGuiaGuiaETodosOsProcedimentosEmOrdemGlobal() {
        prepararGuia();
        var preGuia = new PreGuiaModel();
        preGuia.setId(7L);
        when(preGuiaRepository.findByGuiaId(10L)).thenReturn(Optional.of(preGuia));
        var primeiro = new GuiaProcModel();
        primeiro.setId(20L);
        var segundo = new GuiaProcModel();
        segundo.setId(21L);
        when(guiaProcRepository.findByGuiaModel_Id(10L)).thenReturn(List.of(primeiro, segundo));
        when(historico.executar(EntidadeTipo.PRE_GUIA, 7L)).thenReturn(List.of(
                evento(3, EntidadeTipo.PRE_GUIA, 7, "EMITIDA", 4),
                evento(1, EntidadeTipo.PRE_GUIA, 7, "VALIDADA", 0)));
        when(historico.executar(EntidadeTipo.GUIA, 10L)).thenReturn(List.of(
                evento(2, EntidadeTipo.GUIA, 10, "EMITIDA", 3)));
        when(historico.executar(EntidadeTipo.GUIA_PROC, 20L)).thenReturn(List.of(
                evento(6, EntidadeTipo.GUIA_PROC, 20, "REALIZADA", 8),
                evento(4, EntidadeTipo.GUIA_PROC, 20, "EM_ANDAMENTO", 5)));
        when(historico.executar(EntidadeTipo.GUIA_PROC, 21L)).thenReturn(List.of(
                evento(5, EntidadeTipo.GUIA_PROC, 21, "REALIZADA", 6)));

        var resposta = service.montar(10L);
        assertThat(resposta).extracting("id").containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
        assertThat(resposta).extracting("entidadeTipo").containsExactly(
                EntidadeTipo.PRE_GUIA, EntidadeTipo.GUIA, EntidadeTipo.PRE_GUIA,
                EntidadeTipo.GUIA_PROC, EntidadeTipo.GUIA_PROC, EntidadeTipo.GUIA_PROC);
        assertThat(resposta.get(0).entidadeId()).isEqualTo(7L);
        assertThat(resposta.get(0).statusAnterior()).isEqualTo("PRE_GUIA");
        assertThat(resposta.get(0).statusNovo()).isEqualTo("VALIDADA");
        assertThat(resposta.get(0).origem()).isEqualTo("teste-linha-do-tempo");
        verify(historico).executar(EntidadeTipo.PRE_GUIA, 7L);
        verify(historico).executar(EntidadeTipo.GUIA, 10L);
        verify(historico).executar(EntidadeTipo.GUIA_PROC, 20L);
        verify(historico).executar(EntidadeTipo.GUIA_PROC, 21L);
        verifyNoMoreInteractions(historico);
    }

    @Test
    void datasIguaisSaoDesempatadasPeloIdDoEvento() {
        prepararGuia();
        when(historico.executar(EntidadeTipo.GUIA, 10L)).thenReturn(List.of(
                evento(9, EntidadeTipo.GUIA, 10, "FINALIZADA", 0),
                evento(8, EntidadeTipo.GUIA, 10, "EMITIDA", 0)));
        assertThat(service.montar(10L)).extracting("id").containsExactly(8L, 9L);
    }

    @Test
    void guiaAusenteRetorna404SemLerHistorico() {
        assertThatThrownBy(() -> service.montar(999L))
                .isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(ErrorCode.GUIA_NAO_ENCONTRADA);
        verifyNoInteractions(preGuiaRepository, guiaProcRepository, historico);
    }

    @Test
    void beneficiarioDeOutraGuiaRecebe403AntesDeConsultarEventos() {
        prepararGuia();
        when(usuarioLogado.buscarUsuarioLogado()).thenReturn(usuario("111111111-00", Perfil.BENEFICIARIO));
        assertThatThrownBy(() -> service.montar(10L))
                .isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(ErrorCode.BENEFICIARIO_SEM_PERMISSAO);
        verifyNoInteractions(preGuiaRepository, guiaProcRepository, historico);
    }

    @Test
    void beneficiarioSemPrecCpNaoConsegueConsultar() {
        prepararGuia();
        when(usuarioLogado.buscarUsuarioLogado()).thenReturn(usuario(null, Perfil.BENEFICIARIO));
        assertThatThrownBy(() -> service.montar(10L))
                .isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(ErrorCode.BENEFICIARIO_SEM_PERMISSAO);
        verifyNoInteractions(historico);
    }

    @Test
    void auditorEmissorEChefeConsultamQualquerGuiaMesmoSendoBeneficiarios() {
        prepararGuia();
        for (Perfil perfil : List.of(Perfil.AUDITOR, Perfil.EMISSOR, Perfil.CHEFE)) {
            when(usuarioLogado.buscarUsuarioLogado()).thenReturn(usuario("111111111-00", perfil, Perfil.BENEFICIARIO));
            assertThat(service.montar(10L)).isEmpty();
        }
    }

    @Test
    void ocsNaoTemAcessoMesmoChamandoServiceDiretamente() {
        when(usuarioLogado.buscarUsuarioLogado()).thenReturn(usuario(null, Perfil.OCS));
        assertThatThrownBy(() -> service.montar(10L))
                .isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(ErrorCode.ACESSO_NEGADO);
        verifyNoInteractions(guiaRepository, preGuiaRepository, guiaProcRepository, historico);
    }

    @Test
    void guiaSemEventosOuPreGuiaNaoInventaHistorico() {
        prepararGuia();
        assertThat(service.montar(10L)).isEmpty();
        verify(historico).executar(EntidadeTipo.GUIA, 10L);
        verifyNoMoreInteractions(historico);
    }

    @Test
    void guiaAntigaSemPreGuiaAindaRetornaSeusEventos() {
        prepararGuia();
        when(historico.executar(EntidadeTipo.GUIA, 10L))
                .thenReturn(List.of(evento(1, EntidadeTipo.GUIA, 10, "EMITIDA", 0)));
        assertThat(service.montar(10L)).extracting("entidadeTipo").containsExactly(EntidadeTipo.GUIA);
    }

    private void prepararGuia() {
        var guia = new GuiaModel();
        guia.setId(10L);
        guia.setBenfId("123456789-00");
        when(guiaRepository.findById(10L)).thenReturn(Optional.of(guia));
    }

    private UsuarioLogado usuario(String precCp, Perfil... perfis) {
        return new UsuarioLogado(1, "99351819019", "Teste", Set.of(perfis), precCp,
                null, null, null, null, null, null, null);
    }

    private HistoricoStatusModel evento(long id, EntidadeTipo tipo, long entidadeId, String status, int minuto) {
        var evento = new HistoricoStatusModel(tipo, entidadeId, "PRE_GUIA", status, "teste-linha-do-tempo");
        evento.setId(id);
        evento.setData(LocalDateTime.of(2026, 11, 18, 10, minuto));
        return evento;
    }
}
