package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaProcRepository;
import com.datasphere.fusex.preguia.StatusPreGuia;
import com.datasphere.fusex.preguia.dto.PreGuiaRequest;
import com.datasphere.fusex.preguia.dto.ProcedimentoPreGuiaRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EditarPreGuiaServiceTest {

    @Mock private BuscarPreGuiaPorIdService buscarPreGuiaPorIdService;
    @Mock private BuscarUsuarioLogadoService buscarUsuarioLogadoService;
    @Mock private ValidarDadosDaPreGuiaService validarDadosDaPreGuiaService;
    @Mock private PreGuiaProcRepository preGuiaProcRepository;
    @Mock private SalvarProcedimentosDaPreGuiaService salvarProcedimentosDaPreGuiaService;
    @Mock private RegistrarStatusPreGuiaService registrarStatusPreGuiaService;
    @InjectMocks private EditarPreGuiaService service;

    private final PreGuiaRequest request = new PreGuiaRequest("111111111-00", 2,
            List.of(new ProcedimentoPreGuiaRequest(1, "12345678000199", 4)));

    @BeforeEach
    void autenticarEmissor() {
        when(buscarUsuarioLogadoService.buscarUsuarioLogado()).thenReturn(usuario(Perfil.EMISSOR));
    }

    @Test
    void editaPreGuiaMantendoAutorEDataOriginal() {
        var preGuia = preparar(StatusPreGuia.PRE_GUIA);
        var resposta = service.editar(7L, request);
        assertThat(resposta.id()).isEqualTo(7L);
        assertThat(resposta.status()).isEqualTo(StatusPreGuia.PRE_GUIA);
        assertThat(preGuia.getBenfId()).isEqualTo("111111111-00");
        assertThat(preGuia.getMedId()).isEqualTo(2);
        assertThat(preGuia.getEmissorId()).isEqualTo(9);
        assertThat(preGuia.getDataCriacao()).isEqualTo(LocalDate.of(2026, 1, 1));
        var ordem = inOrder(validarDadosDaPreGuiaService, preGuiaProcRepository, salvarProcedimentosDaPreGuiaService);
        ordem.verify(validarDadosDaPreGuiaService).validar(request);
        ordem.verify(preGuiaProcRepository).findByPreGuiaModel_IdOrderByIdAsc(7L);
        ordem.verify(preGuiaProcRepository).deleteAll(anyIterable());
        ordem.verify(salvarProcedimentosDaPreGuiaService).salvar(preGuia, request.procedimentos());
        verify(buscarPreGuiaPorIdService).buscarPreGuiaParaAtualizar(7L);
    }

    @Test
    void editaComPendenciasEVoltaParaPreGuia() {
        var preGuia = preparar(StatusPreGuia.COM_PENDENCIAS);
        var resposta = service.editar(7L, request);
        assertThat(resposta.status()).isEqualTo(StatusPreGuia.PRE_GUIA);
        verify(registrarStatusPreGuiaService).atualizarStatus(preGuia, StatusPreGuia.PRE_GUIA, "edicao-pre-guia");
    }

    @Test
    void outrosStatusDevolvemConflitoSemAlterarItens() {
        for (var status : List.of(StatusPreGuia.VALIDADA, StatusPreGuia.APROVADA,
                StatusPreGuia.REPROVADA, StatusPreGuia.EMITIDA)) {
            var preGuia = new PreGuiaModel();
            preGuia.setStatus(status);
            when(buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(7L)).thenReturn(preGuia);
            assertThatThrownBy(() -> service.editar(7L, request))
                    .isInstanceOf(BusinessException.class).extracting("errorCode")
                    .isEqualTo(ErrorCode.PRE_GUIA_STATUS_INVALIDO);
        }
        verifyNoInteractions(validarDadosDaPreGuiaService, preGuiaProcRepository,
                salvarProcedimentosDaPreGuiaService, registrarStatusPreGuiaService);
    }

    @Test
    void dadosInvalidosNaoApagamItensExistentes() {
        var preGuia = new PreGuiaModel();
        preGuia.setStatus(StatusPreGuia.PRE_GUIA);
        when(buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(7L)).thenReturn(preGuia);
        doThrow(new BusinessException(ErrorCode.PROCEDIMENTO_SEM_CONTRATO))
                .when(validarDadosDaPreGuiaService).validar(request);
        assertThatThrownBy(() -> service.editar(7L, request)).isInstanceOf(BusinessException.class);
        verifyNoInteractions(preGuiaProcRepository, salvarProcedimentosDaPreGuiaService, registrarStatusPreGuiaService);
    }

    @Test
    void medicoNaoPodeEditarMesmoChamandoOServiceDiretamente() {
        when(buscarUsuarioLogadoService.buscarUsuarioLogado()).thenReturn(usuario(Perfil.MEDICO));
        assertThatThrownBy(() -> service.editar(7L, request))
                .isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(ErrorCode.ACESSO_NEGADO);
        verifyNoInteractions(buscarPreGuiaPorIdService, preGuiaProcRepository);
    }

    @Test
    void preGuiaInexistenteMantem404() {
        when(buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(999L))
                .thenThrow(new BusinessException(ErrorCode.PRE_GUIA_NAO_ENCONTRADA));
        assertThatThrownBy(() -> service.editar(999L, request))
                .isInstanceOf(BusinessException.class).extracting("errorCode")
                .isEqualTo(ErrorCode.PRE_GUIA_NAO_ENCONTRADA);
        verifyNoInteractions(validarDadosDaPreGuiaService, preGuiaProcRepository);
    }

    private PreGuiaModel preparar(StatusPreGuia status) {
        var preGuia = new PreGuiaModel();
        preGuia.setId(7L);
        preGuia.setStatus(status);
        preGuia.setEmissorId(9);
        preGuia.setDataCriacao(LocalDate.of(2026, 1, 1));
        when(buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(7L)).thenReturn(preGuia);
        when(registrarStatusPreGuiaService.atualizarStatus(preGuia, StatusPreGuia.PRE_GUIA, "edicao-pre-guia"))
                .thenAnswer(inv -> { preGuia.setStatus(StatusPreGuia.PRE_GUIA); return preGuia; });
        return preGuia;
    }

    private UsuarioLogado usuario(Perfil perfil) {
        return new UsuarioLogado(1, "18609139034", "Emissor", Set.of(perfil), null,
                null, null, null, 1, null, null, null);
    }
}
