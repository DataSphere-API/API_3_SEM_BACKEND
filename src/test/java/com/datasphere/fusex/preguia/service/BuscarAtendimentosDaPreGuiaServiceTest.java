package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import com.datasphere.fusex.ocs.OcsModel;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import com.datasphere.fusex.procedimento.ProcedimentoModel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class BuscarAtendimentosDaPreGuiaServiceTest {

    private final GuiaProcRepository guiaProcs = mock(GuiaProcRepository.class);
    private final BuscarAtendimentosDaPreGuiaService service = new BuscarAtendimentosDaPreGuiaService(guiaProcs);

    private ProcedimentoModel procedimento(int id) {
        ProcedimentoModel p = new ProcedimentoModel();
        p.setId(id);
        return p;
    }

    private PreGuiaProcModel daPreGuia(long id, PreGuiaModel preGuia, int procedimentoId, String ocs) {
        PreGuiaProcModel p = new PreGuiaProcModel();
        p.setId(id);
        p.setPreGuiaModel(preGuia);
        p.setProcedimentoModel(procedimento(procedimentoId));
        p.setOcsModel(new OcsModel(ocs, "OCS"));
        return p;
    }

    private GuiaProcModel atendimento(long id, int procedimentoId, String ocs) {
        ContratoModel contrato = new ContratoModel();
        contrato.setProcedimentoModel(procedimento(procedimentoId));
        contrato.setOcsModel(new OcsModel(ocs, "OCS"));
        GuiaProcModel a = new GuiaProcModel();
        a.setId(id);
        a.setContratoModel(contrato);
        return a;
    }

    @Test
    void depoisDaEmissaoCadaProcedimentoApontaParaOAtendimentoDaGuia() {
        PreGuiaModel preGuia = new PreGuiaModel();
        preGuia.setGuiaId(9L);
        var raioX = daPreGuia(1L, preGuia, 3, "11111111000111");
        var fisio = daPreGuia(2L, preGuia, 5, "22222222000122");
        when(guiaProcs.findByGuiaModel_Id(9L)).thenReturn(List.of(
                atendimento(70L, 5, "22222222000122"), atendimento(71L, 3, "11111111000111")));

        Map<Long, Long> mapa = service.buscar(List.of(raioX, fisio));

        assertEquals(71L, mapa.get(1L));
        assertEquals(70L, mapa.get(2L));
    }

    @Test
    void antesDaEmissaoNaoHaAtendimento() {
        PreGuiaModel preGuia = new PreGuiaModel();
        Map<Long, Long> mapa = service.buscar(List.of(daPreGuia(1L, preGuia, 3, "11111111000111")));

        assertTrue(mapa.isEmpty());
        verifyNoInteractions(guiaProcs);
    }
}
