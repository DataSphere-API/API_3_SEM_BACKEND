package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guia.dto.DocumentoGuia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarDocumentoDaGuiaServiceTest {

    @Mock
    private GuiaRepository guiaRepository;

    @Mock
    private BuscarUsuarioLogadoService buscarUsuarioLogadoService;

    @InjectMocks
    private BuscarDocumentoDaGuiaService service;

    private void logado(String precCp, Perfil perfil) {
        when(buscarUsuarioLogadoService.buscarUsuarioLogado()).thenReturn(new UsuarioLogado(1, "1", "Teste",
                Set.of(perfil), precCp, null, null, null, null, null, null, null));
    }

    private GuiaModel guiaComDocumento(String dono) {
        GuiaModel guia = new GuiaModel();
        guia.setBenfId(dono);
        guia.setDocumento("%PDF".getBytes());
        guia.setHashPacote("b".repeat(64));
        return guia;
    }

    @Test
    void consultarDuasVezes_devolveSempreOMesmoHashEPdf() {
        logado(null, Perfil.EMISSOR);
        when(guiaRepository.findById(3L)).thenReturn(Optional.of(guiaComDocumento("123456789-00")));

        DocumentoGuia primeira = service.buscar(3L);
        DocumentoGuia segunda = service.buscar(3L);

        assertEquals(primeira.hash(), segunda.hash());
        assertArrayEquals(primeira.pdf(), segunda.pdf());
    }

    @Test
    void beneficiarioDonoBaixaAPropriaGuia() {
        logado("123456789-00", Perfil.BENEFICIARIO);
        when(guiaRepository.findById(3L)).thenReturn(Optional.of(guiaComDocumento("123456789-00")));

        assertEquals("b".repeat(64), service.buscar(3L).hash());
    }

    @Test
    void beneficiarioDeOutraGuia_recebe403() {
        logado("999999999-00", Perfil.BENEFICIARIO);
        when(guiaRepository.findById(3L)).thenReturn(Optional.of(guiaComDocumento("123456789-00")));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.buscar(3L));
        assertEquals(ErrorCode.BENEFICIARIO_SEM_PERMISSAO, ex.getErrorCode());
    }

    @Test
    void guiaInexistente_lancaGuiaNaoEncontrada() {
        when(guiaRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.buscar(99L));
        assertEquals(ErrorCode.GUIA_NAO_ENCONTRADA, ex.getErrorCode());
    }

    @Test
    void guiaEmitidaAntesDoRegistroDoDocumento_lancaDocumentoIndisponivel() {
        logado(null, Perfil.CHEFE);
        when(guiaRepository.findById(1L)).thenReturn(Optional.of(new GuiaModel()));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.buscar(1L));
        assertEquals(ErrorCode.DOCUMENTO_GUIA_INDISPONIVEL, ex.getErrorCode());
    }
}
