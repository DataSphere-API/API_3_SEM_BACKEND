package com.datasphere.fusex.guia.controller;

import com.datasphere.fusex.atendimento.service.ValidarDonoDaGuiaService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.StatusPainelBeneficiario;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaRepository;
import com.datasphere.fusex.preguia.StatusPreGuia;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BeneficiarioPreGuiaControllerTest {

    private final ValidarDonoDaGuiaService dono = mock(ValidarDonoDaGuiaService.class);
    private final PreGuiaRepository preGuias = mock(PreGuiaRepository.class);
    private final BeneficiarioPreGuiaController controller = new BeneficiarioPreGuiaController(dono, preGuias);
    private final Principal principal = () -> "beneficiario";

    @Test
    void listaPreGuiasDoProprioBeneficiarioComoSolicitadas() {
        var preGuia = new PreGuiaModel();
        preGuia.setId(42L);
        when(dono.identificarBeneficiario(principal)).thenReturn("123456789-00");
        when(preGuias.findByBenfIdAndGuiaIdIsNullAndStatusNotOrderByIdDesc(
                "123456789-00", StatusPreGuia.EMITIDA)).thenReturn(List.of(preGuia));

        var resposta = controller.listar("123456789-00", principal).getFirst();

        assertEquals(42L, resposta.preGuiaId());
        assertEquals(StatusPainelBeneficiario.SOLICITADA, resposta.statusPainel());
    }

    @Test
    void recusaOutroBeneficiarioAntesDeConsultarPreGuias() {
        when(dono.identificarBeneficiario(principal)).thenReturn("123456789-00");

        var erro = assertThrows(BusinessException.class, () -> controller.listar("123456789-01", principal));

        assertEquals(ErrorCode.BENEFICIARIO_SEM_PERMISSAO, erro.getErrorCode());
        assertEquals(403, erro.getErrorCode().getStatus().value());
        verifyNoInteractions(preGuias);
    }
}
