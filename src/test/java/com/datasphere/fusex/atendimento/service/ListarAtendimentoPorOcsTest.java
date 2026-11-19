package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.ocs.OcsModel;
import com.datasphere.fusex.procedimento.ProcedimentoModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarAtendimentosPorOcsTest {

    @Mock private GuiaProcRepository guiaProcRepository;
    @Mock private BuscarOcsAutenticadaService buscarOcsAutenticadaService;
    @Mock private EspelhoItemRepository espelhoItemRepository;
    @Mock private EspelhoRepository espelhoRepository;

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

    @Test
    void atendimentoEmEspelhoVemComOEspelhoId() {
        ProcedimentoModel procedimento = new ProcedimentoModel();
        procedimento.setNome("Fisioterapia");
        ContratoModel contrato = new ContratoModel();
        contrato.setOcsModel(new OcsModel("12345678000199", "Clinica"));
        contrato.setProcedimentoModel(procedimento);
        BeneficiarioModel beneficiario = new BeneficiarioModel();
        beneficiario.setPrecCP("123456789-00");
        beneficiario.setNome("Ricardo");
        GuiaModel guia = new GuiaModel();
        guia.setId(3L);
        guia.setBeneficiarioModel(beneficiario);
        GuiaProcModel atendimento = new GuiaProcModel();
        atendimento.setId(10L);
        atendimento.setGuiaModel(guia);
        atendimento.setContratoModel(contrato);
        atendimento.setStatus("REALIZADA");
        EspelhoItemModel item = new EspelhoItemModel();
        item.setEspelhoId(4);
        item.setAtendimentoId(10L);
        EspelhoModel espelho = new EspelhoModel();
        espelho.setId(4);
        espelho.setStatus(StatusEspelho.EM_ABERTO);
        when(buscarOcsAutenticadaService.buscarOcsAutenticada()).thenReturn("12345678000199");
        when(guiaProcRepository.findByContratoModel_OcsModel_Cnpj("12345678000199")).thenReturn(List.of(atendimento));
        when(espelhoItemRepository.findByAtendimentoIdIn(List.of(10L))).thenReturn(List.of(item));
        when(espelhoRepository.findById(4)).thenReturn(Optional.of(espelho));

        assertEquals(4, service.listarAtendimentosPorOcs("12345678000199").get(0).espelhoId());
    }
}
