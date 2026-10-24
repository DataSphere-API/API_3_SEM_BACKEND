package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecalcularStatusDaGuiaServiceTest {

    @Mock
    private GuiaProcRepository guiaProcRepository;
    @Mock
    private GuiaRepository guiaRepository;
    @Mock
    private RegistrarMudancaStatusService registrarMudancaStatusService;

    @InjectMocks
    private RecalcularStatusDaGuiaService service;

    private GuiaProcModel proc(String status) {
        GuiaProcModel p = new GuiaProcModel();
        p.setStatus(status);
        return p;
    }

    private GuiaModel guia(String status) {
        GuiaModel g = new GuiaModel();
        g.setId(11L);
        g.setStatus(status);
        return g;
    }

    @Test
    void todosRealizados_guiaFicaFinalizada() {
        when(guiaRepository.findByIdParaAtualizar(11L)).thenReturn(Optional.of(guia("EMITIDA")));
        when(guiaProcRepository.findByGuiaModel_Id(11L)).thenReturn(List.of(proc("Realizada"), proc("Realizada")));

        assertEquals("FINALIZADA", service.recalcularStatusDaGuia(11L));

        verify(guiaRepository).atualizarStatus(11L, "FINALIZADA");
        verify(registrarMudancaStatusService)
                .executar(EntidadeTipo.GUIA, 11L, "EMITIDA", "FINALIZADA", "validacao-qrcode");
    }

    @Test
    void umProcedimentoPendente_guiaFicaEmitida() {
        when(guiaRepository.findByIdParaAtualizar(11L)).thenReturn(Optional.of(guia("EMITIDA")));
        when(guiaProcRepository.findByGuiaModel_Id(11L)).thenReturn(List.of(proc("Realizada"), proc("Pendente")));

        assertEquals("EMITIDA", service.recalcularStatusDaGuia(11L));

        verify(guiaRepository, never()).atualizarStatus(anyLong(), anyString());
        verifyNoInteractions(registrarMudancaStatusService);
    }

    @Test
    void statusJaCorreto_naoGravaNemRegistraHistorico() {
        when(guiaRepository.findByIdParaAtualizar(11L)).thenReturn(Optional.of(guia("FINALIZADA")));
        when(guiaProcRepository.findByGuiaModel_Id(11L)).thenReturn(List.of(proc("Realizada")));

        assertEquals("FINALIZADA", service.recalcularStatusDaGuia(11L));

        verify(guiaRepository, never()).atualizarStatus(anyLong(), anyString());
        verifyNoInteractions(registrarMudancaStatusService);
    }
}