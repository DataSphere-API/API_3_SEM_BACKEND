package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemRepository;
import com.datasphere.fusex.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoverItemDoEspelhoServiceTest {

    @Mock private EspelhoItemRepository espelhoItemRepository;
    @Mock private EspelhoRepository espelhoRepository;
    @Mock private BuscarEspelhoPorIdService buscarEspelhoPorIdService;
    @InjectMocks private RemoverItemDoEspelhoService service;

    private EspelhoModel espelho(StatusEspelho status) {
        EspelhoModel e = new EspelhoModel();
        e.setId(1);
        e.setStatus(status);
        return e;
    }

    @Test
    void espelhoReprovado_permiteRemoverQuandoHaMaisDeUmItem() {
        when(buscarEspelhoPorIdService.buscarEspelhoPorId(1)).thenReturn(espelho(StatusEspelho.REPROVADO));
        EspelhoItemModel item = new EspelhoItemModel();
        when(espelhoItemRepository.findByIdAndEspelhoId(2, 1)).thenReturn(Optional.of(item));
        when(espelhoItemRepository.countByEspelhoId(1)).thenReturn(2L);

        assertTrue(service.removerItemDoEspelho(1, 2));
        verify(espelhoItemRepository).delete(item);
    }

    @Test
    void espelhoFaturado_naoPermiteRemover() {
        when(buscarEspelhoPorIdService.buscarEspelhoPorId(1)).thenReturn(espelho(StatusEspelho.FATURADO));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.removerItemDoEspelho(1, 2));
        assertEquals(ErrorCode.ESPELHO_NAO_EDITAVEL, ex.getErrorCode());
        verify(espelhoItemRepository, never()).delete(any());
    }

    @Test
    void ultimoItem_devolveEspelhoSemItensENaoRemove() {
        when(buscarEspelhoPorIdService.buscarEspelhoPorId(1)).thenReturn(espelho(StatusEspelho.EM_ABERTO));
        when(espelhoItemRepository.findByIdAndEspelhoId(2, 1)).thenReturn(Optional.of(new EspelhoItemModel()));
        when(espelhoItemRepository.countByEspelhoId(1)).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.removerItemDoEspelho(1, 2));
        assertEquals(ErrorCode.ESPELHO_SEM_ITENS, ex.getErrorCode());
        verify(espelhoItemRepository, never()).delete(any());
    }
}
