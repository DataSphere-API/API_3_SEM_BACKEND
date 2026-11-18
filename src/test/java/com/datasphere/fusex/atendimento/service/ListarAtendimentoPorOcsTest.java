package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarAtendimentosPorOcsTest {

    @Mock private GuiaProcRepository guiaProcRepository;
    @Mock private BuscarOcsAutenticadaService buscarOcsAutenticadaService;

    @InjectMocks
    private ListarAtendimentosPorOcs service;

    @Test
    void ocsDeOutroCnpj_lancaOcsDiferente() {
        when(buscarOcsAutenticadaService.buscarOcsAutenticada()).thenReturn("12345678000199");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.listarAtendimentosPorOcs("98765432000111"));

        assertEquals(ErrorCode.OCS_DIFERENTE_DO_CONTRATO, ex.getErrorCode());
        verifyNoInteractions(guiaProcRepository);
    }

    @Test
    void propriaOcs_listaOsAtendimentos() {
        when(buscarOcsAutenticadaService.buscarOcsAutenticada()).thenReturn("12345678000199");
        when(guiaProcRepository.findByContratoModel_OcsModel_Cnpj("12345678000199")).thenReturn(List.of());

        assertTrue(service.listarAtendimentosPorOcs("12345678000199").isEmpty());
    }
}
