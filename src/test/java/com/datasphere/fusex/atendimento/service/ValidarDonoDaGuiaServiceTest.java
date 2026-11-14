package com.datasphere.fusex.atendimento.service;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidarDonoDaGuiaServiceTest {

    @Mock private BeneficiarioRepository repository;
    @InjectMocks private ValidarDonoDaGuiaService service;

    @Test
    void identificaBeneficiarioPeloLoginAutenticado() {
        var beneficiario = new BeneficiarioModel();
        beneficiario.setPrecCP("dono");
        when(repository.findByLogin("cpf-login")).thenReturn(Optional.of(beneficiario));
        assertEquals("dono", service.identificarBeneficiario(() -> "cpf-login"));
    }

    @Test
    void semPrincipalRetorna401SemConsultarBanco() {
        var erro = assertThrows(BusinessException.class, () -> service.identificarBeneficiario(null));
        assertEquals(ErrorCode.AUTENTICACAO_OBRIGATORIA, erro.getErrorCode());
        verifyNoInteractions(repository);
    }

    @Test
    void loginSemBeneficiarioNaoTemAcesso() {
        when(repository.findByLogin("ocs")).thenReturn(Optional.empty());
        var erro = assertThrows(BusinessException.class, () -> service.identificarBeneficiario(() -> "ocs"));
        assertEquals(ErrorCode.BENEFICIARIO_SEM_PERMISSAO, erro.getErrorCode());
    }

    @Test
    void compararDonoNaoAceitaOutroPrecCpOuIdentidadeNula() {
        var guia = new GuiaModel();
        guia.setBenfId("dono");
        assertDoesNotThrow(() -> service.validarDonoDaGuia(guia, "dono"));
        for (String precCp : new String[]{"outro", null}) {
            var erro = assertThrows(BusinessException.class, () -> service.validarDonoDaGuia(guia, precCp));
            assertEquals(ErrorCode.BENEFICIARIO_SEM_PERMISSAO, erro.getErrorCode());
        }
    }
}
