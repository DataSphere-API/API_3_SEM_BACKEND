package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.atendimento.service.GerarCodigoQrService;
import com.datasphere.fusex.atendimento.service.GerarCodigoUnicoQrService;
import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.service.ValidadeGuiaService;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarProcedimentosDaGuiaServiceTest {

    @Mock private GuiaProcRepository repository;
    @Mock private BuscarContratoDaPreGuiaProcService buscarContrato;
    @Mock private RegistrarMudancaStatusService historico;

    @Test
    void guiaComTresProcedimentosRecebeTresUuidAntesDeSalvar() {
        var service = new CriarProcedimentosDaGuiaService(repository, buscarContrato, new ValidadeGuiaService(60),
                historico, new GerarCodigoQrService(new GerarCodigoUnicoQrService()));
        var primeiro = new PreGuiaProcModel();
        var segundo = new PreGuiaProcModel();
        var terceiro = new PreGuiaProcModel();
        for (var procedimento : List.of(primeiro, segundo, terceiro)) procedimento.setQtdAutorizada(2);
        when(buscarContrato.buscarContrato(any())).thenReturn(new ContratoModel());
        AtomicLong ids = new AtomicLong();
        when(repository.save(any(GuiaProcModel.class))).thenAnswer(invocacao -> {
            GuiaProcModel procedimento = invocacao.getArgument(0);
            assertNotNull(UUID.fromString(procedimento.getQr()));
            procedimento.setId(ids.incrementAndGet());
            return procedimento;
        });
        var salvos = service.criarProcedimentos(new GuiaModel(), List.of(primeiro, segundo, terceiro), LocalDate.now());
        assertEquals(3, salvos.size());
        assertEquals(3, salvos.stream().map(GuiaProcModel::getQr).distinct().count());
        for (var procedimento : salvos) {
            assertEquals("PENDENTE", procedimento.getStatus());
            assertEquals(0, procedimento.getQtdRealizada());
        }
        verify(repository, times(3)).save(any());
    }
}
