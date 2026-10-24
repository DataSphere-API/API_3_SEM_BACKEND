package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guia.dto.request.ExpiracaoResponse;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpirarGuiasServiceTest {

    @Mock
    private GuiaRepository guiaRepository;
    @Mock
    private GuiaProcRepository guiaProcRepository;
    @Mock
    private ValidadeGuiaService validadeGuiaService;
    @Mock
    private RegistrarMudancaStatusService registrarMudancaStatusService;

    @InjectMocks
    private ExpirarGuiasService service;

    private GuiaProcModel procedimento(Long id, String status) {
        GuiaProcModel p = new GuiaProcModel();
        p.setGuiaId(id);
        p.setStatus(status);
        return p;
    }

    @Test
    void guiaVencida_expiraSoOsProcedimentosNaoRealizados() {
        LocalDate limite = LocalDate.now().minusDays(60);
        GuiaModel guia = new GuiaModel();
        guia.setId(1L);
        guia.setStatus("EMITIDA");
        GuiaProcModel pendente = procedimento(10L, "PENDENTE");
        GuiaProcModel realizado = procedimento(11L, "REALIZADA");

        when(validadeGuiaService.calcularLimiteDeEmissaoVencida()).thenReturn(limite);
        when(guiaRepository.findByStatusAndDataEmissaoBefore("EMITIDA", limite)).thenReturn(List.of(guia));
        when(guiaProcRepository.findByGuiaModel_Id(1L)).thenReturn(List.of(pendente, realizado));

        ExpiracaoResponse resposta = service.expirarGuiasVencidas();

        assertEquals(1, resposta.guiasExpiradas());
        assertEquals(1, resposta.procedimentosExpirados());
        assertEquals("EXPIRADA", pendente.getStatus());
        assertEquals("REALIZADA", realizado.getStatus());
        assertEquals("EXPIRADA", guia.getStatus());
        verify(guiaProcRepository, never()).save(realizado);
        verify(registrarMudancaStatusService)
                .executar(EntidadeTipo.GUIA_PROC, 10L, "PENDENTE", "EXPIRADA", "expiracao-de-guias");
        verify(registrarMudancaStatusService)
                .executar(EntidadeTipo.GUIA, 1L, "EMITIDA", "EXPIRADA", "expiracao-de-guias");
    }

    @Test
    void semGuiasVencidas_naoAlteraNada() {
        LocalDate limite = LocalDate.now().minusDays(60);
        when(validadeGuiaService.calcularLimiteDeEmissaoVencida()).thenReturn(limite);
        when(guiaRepository.findByStatusAndDataEmissaoBefore("EMITIDA", limite)).thenReturn(List.of());

        ExpiracaoResponse resposta = service.expirarGuiasVencidas();

        assertEquals(0, resposta.guiasExpiradas());
        assertEquals(0, resposta.procedimentosExpirados());
    }
}