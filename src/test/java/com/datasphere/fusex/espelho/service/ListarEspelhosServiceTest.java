package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListarEspelhosServiceTest {

    @Mock private EspelhoRepository espelhoRepository;
    @Mock private EnriquecerListaEspelhosService enriquecerListaEspelhosService;
    @Mock private BuscarUsuarioLogadoService buscarUsuarioLogadoService;

    @InjectMocks
    private ListarEspelhosService service;

    private UsuarioLogado usuario(Perfil perfil, String ocsCnpj) {
        return new UsuarioLogado(1, "11111111111", "Teste", Set.of(perfil), null,
                null, null, null, null, null, ocsCnpj, null);
    }

    private EspelhoModel espelho(int id, StatusEspelho status) {
        EspelhoModel espelho = new EspelhoModel();
        espelho.setId(id);
        espelho.setStatus(status);
        return espelho;
    }

    @Test
    void ocsSemFiltroVeSoOsDela() {
        when(buscarUsuarioLogadoService.buscarUsuarioLogado()).thenReturn(usuario(Perfil.OCS, "12345678000199"));
        when(espelhoRepository.findByOcsId("12345678000199")).thenReturn(List.of());

        service.listar(null, null);

        verify(espelhoRepository).findByOcsId("12345678000199");
        verify(espelhoRepository, never()).findAll();
    }

    @Test
    void ocsPedindoOutroCnpjRecebe403() {
        when(buscarUsuarioLogadoService.buscarUsuarioLogado()).thenReturn(usuario(Perfil.OCS, "12345678000199"));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.listar("99999999000199", null));
        assertEquals(ErrorCode.OCS_DIFERENTE_DO_CONTRATO, ex.getErrorCode());
        verifyNoInteractions(espelhoRepository);
    }

    @Test
    void auditorFiltraPorStatus() {
        when(buscarUsuarioLogadoService.buscarUsuarioLogado()).thenReturn(usuario(Perfil.AUDITOR, null));
        when(espelhoRepository.findAll()).thenReturn(List.of(espelho(1, StatusEspelho.EM_ABERTO), espelho(2, StatusEspelho.FATURADO)));

        service.listar(null, StatusEspelho.EM_ABERTO);

        verify(enriquecerListaEspelhosService).enriquecer(argThat(lista -> lista.size() == 1 && lista.get(0).getId() == 1));
    }
}
