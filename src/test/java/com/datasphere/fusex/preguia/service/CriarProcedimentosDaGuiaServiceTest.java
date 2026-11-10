package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.service.ValidadeGuiaService;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.service.RegistrarMudancaStatusService;
import com.datasphere.fusex.preguia.PreGuiaProcModel;
import com.datasphere.fusex.preguia.PreGuiaProcRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarProcedimentosDaGuiaServiceTest {

    @Mock
    private GuiaProcRepository guiaProcRepository;
    @Mock
    private PreGuiaProcRepository preGuiaProcRepository;
    @Mock
    private BuscarContratoDaPreGuiaProcService buscarContratoDaPreGuiaProcService;
    @Mock
    private ValidadeGuiaService validadeGuiaService;
    @Mock
    private RegistrarMudancaStatusService registrarMudancaStatusService;

    @InjectMocks
    private CriarProcedimentosDaGuiaService service;

    private PreGuiaProcModel procedimentoDaPreGuia(Long id) {
        PreGuiaProcModel p = new PreGuiaProcModel();
        p.setId(id);
        p.setQtdAutorizada(3);
        return p;
    }

    @Test
    void emissao_gravaOVinculoComOGuiaProcEORegistraOHistoricoDoAtendimentoCorreto() {
        GuiaModel guia = new GuiaModel();
        guia.setId(4L);
        PreGuiaProcModel pgp = procedimentoDaPreGuia(5L);
        LocalDate emissao = LocalDate.of(2026, 10, 1);

        when(buscarContratoDaPreGuiaProcService.buscarContrato(pgp)).thenReturn(new ContratoModel());
        when(validadeGuiaService.calcularDataValidade(emissao)).thenReturn(emissao.plusDays(60));
        when(guiaProcRepository.save(any(GuiaProcModel.class))).thenAnswer(inv -> {
            GuiaProcModel g = inv.getArgument(0);
            g.setGuiaId(99L);
            return g;
        });

        List<GuiaProcModel> criados = service.criarProcedimentos(guia, List.of(pgp), emissao);

        assertEquals(1, criados.size());
        assertEquals(99L, pgp.getGuiaProcId());
        verify(preGuiaProcRepository).save(pgp);
        verify(registrarMudancaStatusService)
                .executar(EntidadeTipo.GUIA_PROC, 99L, null, "PENDENTE", "emissao-da-guia");
    }

    @Test
    void procedimentoSemContrato_lancaErroENaoGravaNada() {
        PreGuiaProcModel pgp = procedimentoDaPreGuia(5L);
        when(buscarContratoDaPreGuiaProcService.buscarContrato(pgp)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.criarProcedimentos(new GuiaModel(), List.of(pgp), LocalDate.now()));

        assertEquals(ErrorCode.PROCEDIMENTO_SEM_CONTRATO, ex.getErrorCode());
        verifyNoInteractions(guiaProcRepository, preGuiaProcRepository, registrarMudancaStatusService);
    }
}