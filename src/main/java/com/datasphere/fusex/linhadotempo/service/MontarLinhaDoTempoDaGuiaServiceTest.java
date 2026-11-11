package com.datasphere.fusex.linhadotempo.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.linhadotempo.dto.EtapaLinhaDoTempo;
import com.datasphere.fusex.linhadotempo.dto.EventoLinhaDoTempoResponse;
import com.datasphere.fusex.linhadotempo.dto.LinhaDoTempoResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MontarLinhaDoTempoDaGuiaServiceTest {

    @Mock
    private GuiaRepository guiaRepository;
    @Mock
    private GuiaProcRepository guiaProcRepository;
    @Mock
    private BuscarEventosDaPreGuiaService buscarEventosDaPreGuiaService;
    @Mock
    private BuscarEventosDaGuiaEAtendimentosService buscarEventosDaGuiaEAtendimentosService;
    @Mock
    private BuscarEventosDoFaturamentoService buscarEventosDoFaturamentoService;

    @InjectMocks
    private MontarLinhaDoTempoDaGuiaService service;

    private EventoLinhaDoTempoResponse evento(EtapaLinhaDoTempo etapa, EntidadeTipo tipo, LocalDateTime data) {
        return new EventoLinhaDoTempoResponse(etapa, tipo, 1L, null, "X", "desc", data, "origem");
    }

    @Test
    void guiaInexistente_lancaGuiaNaoEncontrada() {
        when(guiaRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.montar(99L));

        assertEquals(ErrorCode.GUIA_NAO_ENCONTRADA, ex.getErrorCode());
    }

    @Test
    void juntaAsFontesEOrdenaPorData() {
        GuiaModel guia = new GuiaModel();
        guia.setId(3L);
        guia.setStatus("EMITIDA");
        guia.setDataEmissao(LocalDate.of(2026, 11, 10));

        GuiaProcModel proc = new GuiaProcModel();
        proc.setGuiaId(30L);

        LocalDateTime t1 = LocalDateTime.of(2026, 11, 8, 9, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 11, 10, 9, 0);
        LocalDateTime t3 = LocalDateTime.of(2026, 11, 12, 9, 0);

        when(guiaRepository.findById(3L)).thenReturn(Optional.of(guia));
        when(guiaProcRepository.findByGuiaModel_Id(3L)).thenReturn(List.of(proc));
        when(buscarEventosDaPreGuiaService.buscar(3L))
                .thenReturn(List.of(evento(EtapaLinhaDoTempo.SOLICITACAO, EntidadeTipo.PRE_GUIA, t1)));
        when(buscarEventosDaGuiaEAtendimentosService.buscar(any(), any()))
                .thenReturn(List.of(evento(EtapaLinhaDoTempo.ATENDIMENTO, EntidadeTipo.GUIA_PROC, t3),
                        evento(EtapaLinhaDoTempo.EMISSAO, EntidadeTipo.GUIA, t2)));
        when(buscarEventosDoFaturamentoService.buscar(List.of(30L))).thenReturn(List.of());

        LinhaDoTempoResponse resposta = service.montar(3L);

        assertEquals(3L, resposta.guiaId());
        assertEquals("EMITIDA", resposta.statusAtual());
        assertEquals(3, resposta.eventos().size());
        assertEquals(EtapaLinhaDoTempo.SOLICITACAO, resposta.eventos().get(0).etapa());
        assertEquals(EtapaLinhaDoTempo.EMISSAO, resposta.eventos().get(1).etapa());
        assertEquals(EtapaLinhaDoTempo.ATENDIMENTO, resposta.eventos().get(2).etapa());
    }
}