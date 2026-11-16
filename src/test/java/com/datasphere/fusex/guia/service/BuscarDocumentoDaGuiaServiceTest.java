package com.datasphere.fusex.guia.service;

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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarDocumentoDaGuiaServiceTest {

    @Mock
    private GuiaRepository guiaRepository;

    @InjectMocks
    private BuscarDocumentoDaGuiaService service;

    @Test
    void consultarDuasVezes_devolveSempreOMesmoHashEPdf() {
        GuiaModel guia = new GuiaModel();
        guia.setDocumento("%PDF".getBytes());
        guia.setHashPacote("b".repeat(64));
        when(guiaRepository.findById(3L)).thenReturn(Optional.of(guia));

        DocumentoGuia primeira = service.buscar(3L);
        DocumentoGuia segunda = service.buscar(3L);

        assertEquals(primeira.hash(), segunda.hash());
        assertArrayEquals(primeira.pdf(), segunda.pdf());
    }

    @Test
    void guiaInexistente_lancaGuiaNaoEncontrada() {
        when(guiaRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.buscar(99L));
        assertEquals(ErrorCode.GUIA_NAO_ENCONTRADA, ex.getErrorCode());
    }

    @Test
    void guiaEmitidaAntesDaV14_lancaDocumentoIndisponivel() {
        when(guiaRepository.findById(1L)).thenReturn(Optional.of(new GuiaModel()));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.buscar(1L));
        assertEquals(ErrorCode.DOCUMENTO_GUIA_INDISPONIVEL, ex.getErrorCode());
    }
}
