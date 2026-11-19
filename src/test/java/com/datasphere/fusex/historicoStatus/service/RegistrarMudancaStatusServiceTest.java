package com.datasphere.fusex.historicoStatus.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.HistoricoStatusModel;
import com.datasphere.fusex.historicoStatus.HistoricoStatusRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarMudancaStatusServiceTest {

    @Mock private HistoricoStatusRepository repository;
    @InjectMocks private RegistrarMudancaStatusService service;

    @BeforeEach
    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void eventoAutenticadoGuardaIdENomeCopiados() {
        autenticar(7, "Roberto Lima");
        registrar();
        var evento = eventoSalvo();
        assertThat(evento.getUsuarioId()).isEqualTo(7);
        assertThat(evento.getUsuarioNome()).isEqualTo("Roberto Lima");
        assertThat(evento.getEntidadeTipo()).isEqualTo(EntidadeTipo.PRE_GUIA);
        assertThat(evento.getEntidadeId()).isEqualTo(10L);
        assertThat(evento.getStatusAnterior()).isEqualTo("VALIDADA");
        assertThat(evento.getStatusNovo()).isEqualTo("APROVADA");
        assertThat(evento.getOrigem()).isEqualTo("aprovacao-chefe");
        assertThat(evento.getData()).isNotNull();
    }

    @Test
    void eventoSemLoginMantemUsuarioNulo() {
        registrar();
        var evento = eventoSalvo();
        assertThat(evento.getUsuarioId()).isNull();
        assertThat(evento.getUsuarioNome()).isNull();
    }

    @Test
    void autenticacaoAnonimaNaoViraUsuarioDoEvento() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "teste", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
        registrar();
        assertThat(eventoSalvo().getUsuarioId()).isNull();
    }

    @Test
    void principalNaoAutenticadoNaoEGravado() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(usuario(7, "Chefe"), null));
        registrar();
        var evento = eventoSalvo();
        assertThat(evento.getUsuarioId()).isNull();
        assertThat(evento.getUsuarioNome()).isNull();
    }

    @Test
    void principalDeOutroTipoNaoQuebraRegistroAutomatico() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("outro-principal", null, List.of()));
        registrar();
        assertThat(eventoSalvo().getUsuarioId()).isNull();
    }

    @Test
    void proximoEventoSemLoginNaoReutilizaUsuarioAnterior() {
        autenticar(7, "Chefe");
        registrar();
        SecurityContextHolder.clearContext();
        registrar();
        var captor = ArgumentCaptor.forClass(HistoricoStatusModel.class);
        verify(repository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getUsuarioId()).isEqualTo(7);
        assertThat(captor.getAllValues().get(1).getUsuarioId()).isNull();
        assertThat(captor.getAllValues().get(1).getUsuarioNome()).isNull();
    }

    @Test
    void trocarUsuarioNaoAlteraNomeDeEventoJaRegistrado() {
        autenticar(7, "Nome original");
        registrar();
        var evento = eventoSalvo();
        autenticar(8, "Outro nome");
        assertThat(evento.getUsuarioId()).isEqualTo(7);
        assertThat(evento.getUsuarioNome()).isEqualTo("Nome original");
    }

    @Test
    void threadDeJobNaoHerdaUsuarioDaRequisicao() throws Exception {
        autenticar(7, "Chefe");
        try (var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> service.executar(EntidadeTipo.GUIA, 2L, "EMITIDA", "EXPIRADA", "expiracao-de-guias"))
                    .get(10, TimeUnit.SECONDS);
        }
        var evento = eventoSalvo();
        assertThat(evento.getUsuarioId()).isNull();
        assertThat(evento.getUsuarioNome()).isNull();
        assertThat(evento.getStatusNovo()).isEqualTo("EXPIRADA");
    }

    @Test
    void responseDevolveUsuarioEPreservaCamposExistentes() {
        autenticar(7, "Chefe");
        registrar();
        var evento = eventoSalvo();
        evento.setId(99L);
        var resposta = HistoricoStatusResponse.from(evento);
        assertThat(resposta.id()).isEqualTo(99L);
        assertThat(resposta.usuarioId()).isEqualTo(7);
        assertThat(resposta.usuarioNome()).isEqualTo("Chefe");
        assertThat(resposta.statusNovo()).isEqualTo("APROVADA");
        assertThat(resposta.origem()).isEqualTo("aprovacao-chefe");
        assertThat(resposta.data()).isEqualTo(evento.getData());
    }

    @Test
    void responseDeEventoAntigoContinuaSemUsuario() {
        var evento = new HistoricoStatusModel(EntidadeTipo.GUIA, 1L, null, "EMITIDA", "emissao-da-guia");
        var resposta = HistoricoStatusResponse.from(evento);
        assertThat(resposta.usuarioId()).isNull();
        assertThat(resposta.usuarioNome()).isNull();
    }

    @Test
    void falhaAoPersistirNaoESilenciada() {
        when(repository.save(any(HistoricoStatusModel.class))).thenThrow(new IllegalStateException("banco indisponível"));
        assertThatThrownBy(this::registrar).isInstanceOf(IllegalStateException.class).hasMessage("banco indisponível");
    }

    private void registrar() {
        service.executar(EntidadeTipo.PRE_GUIA, 10L, "VALIDADA", "APROVADA", "aprovacao-chefe");
    }

    private HistoricoStatusModel eventoSalvo() {
        var captor = ArgumentCaptor.forClass(HistoricoStatusModel.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    private void autenticar(int id, String nome) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(usuario(id, nome), null, List.of()));
    }

    private UsuarioLogado usuario(int id, String nome) {
        return new UsuarioLogado(id, "52601815906", nome, Set.of(Perfil.CHEFE), "444444444-00",
                null, null, 1, null, null, null, null);
    }
}
