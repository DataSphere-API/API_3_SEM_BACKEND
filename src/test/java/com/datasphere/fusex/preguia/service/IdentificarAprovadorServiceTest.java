package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.service.IdentificarAprovadorService.Aprovador;
import com.datasphere.fusex.preguia.service.IdentificarAprovadorService.Papel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentificarAprovadorServiceTest {

    @Mock
    private BuscarUsuarioLogadoService buscarUsuarioLogadoService;

    @InjectMocks
    private IdentificarAprovadorService service;

    private PreGuiaModel preGuiaDoMedico(int medicoId) {
        PreGuiaModel p = new PreGuiaModel();
        p.setMedId(medicoId);
        return p;
    }

    private void logar(EnumSet<Perfil> perfis, Integer medicoId, Integer chefeId) {
        UsuarioLogado usuario = new UsuarioLogado(10, "08301661305", "Carlos Medeiros", perfis, "222222222-00",
                medicoId, "12345", chefeId, null, null, null, null);
        when(buscarUsuarioLogadoService.buscarUsuarioLogado()).thenReturn(usuario);
    }

    @Test
    void medicoResponsavel_assinaComoMedico() {
        logar(EnumSet.of(Perfil.MEDICO), 1, null);

        Aprovador aprovador = service.identificar(preGuiaDoMedico(1));

        assertEquals(Papel.MEDICO, aprovador.papel());
        assertEquals(10, aprovador.usuarioId());
        assertEquals("Carlos Medeiros", aprovador.nome());
    }

    @Test
    void outroMedico_lancaMedicoNaoResponsavel() {
        logar(EnumSet.of(Perfil.MEDICO), 2, null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.identificar(preGuiaDoMedico(1)));

        assertEquals(ErrorCode.MEDICO_NAO_RESPONSAVEL, ex.getErrorCode());
    }

    @Test
    void chefe_assinaComoChefe() {
        logar(EnumSet.of(Perfil.CHEFE), null, 3);

        Aprovador aprovador = service.identificar(preGuiaDoMedico(1));

        assertEquals(Papel.CHEFE, aprovador.papel());
        assertEquals(3, aprovador.id());
    }

    @Test
    void medicoQueTambemEChefe_naoResponsavel_assinaComoChefe() {
        logar(EnumSet.of(Perfil.MEDICO, Perfil.CHEFE), 2, 3);

        Aprovador aprovador = service.identificar(preGuiaDoMedico(1));

        assertEquals(Papel.CHEFE, aprovador.papel());
    }

    @Test
    void emissor_lancaPerfilSemPermissao() {
        logar(EnumSet.of(Perfil.EMISSOR), null, null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.identificar(preGuiaDoMedico(1)));

        assertEquals(ErrorCode.PERFIL_SEM_PERMISSAO, ex.getErrorCode());
    }
}
