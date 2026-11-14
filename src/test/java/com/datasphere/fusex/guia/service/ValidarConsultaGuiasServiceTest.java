package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.analista.AnalistaModel;
import com.datasphere.fusex.analista.AnalistaRepository;
import com.datasphere.fusex.atendimento.service.ValidarDonoDaGuiaService;
import com.datasphere.fusex.chefe.ChefeFusexModel;
import com.datasphere.fusex.chefe.ChefeFusexRepository;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidarConsultaGuiasServiceTest {

    @Mock private ValidarDonoDaGuiaService identificar;
    @Mock private ChefeFusexRepository chefeRepository;
    @Mock private AnalistaRepository analistaRepository;
    @InjectMocks private ValidarConsultaGuiasService service;
    private final Principal principal = () -> "login";

    @BeforeEach
    void configurar() {
        when(identificar.identificarBeneficiario(principal)).thenReturn("autenticado");
    }

    @Test
    void donoConsultaSemPrecisarDePerfilPrivilegiado() {
        assertDoesNotThrow(() -> service.validarConsultaGuias("autenticado", principal));
        verifyNoInteractions(chefeRepository, analistaRepository);
    }

    @Test
    void chefePodeConsultarOutroBeneficiario() {
        when(chefeRepository.findByBenefId("autenticado")).thenReturn(Optional.of(new ChefeFusexModel()));
        assertDoesNotThrow(() -> service.validarConsultaGuias("outro", principal));
        verifyNoInteractions(analistaRepository);
    }

    @Test
    void emissorPodeConsultarOutroBeneficiario() {
        when(chefeRepository.findByBenefId("autenticado")).thenReturn(Optional.empty());
        when(analistaRepository.findByBenefId("autenticado")).thenReturn(Optional.of(new AnalistaModel()));
        assertDoesNotThrow(() -> service.validarConsultaGuias("outro", principal));
    }

    @Test
    void terceiroSemPerfilPrivilegiadoRecebe403() {
        when(chefeRepository.findByBenefId("autenticado")).thenReturn(Optional.empty());
        when(analistaRepository.findByBenefId("autenticado")).thenReturn(Optional.empty());
        var erro = assertThrows(BusinessException.class, () -> service.validarConsultaGuias("outro", principal));
        assertEquals(ErrorCode.BENEFICIARIO_SEM_PERMISSAO, erro.getErrorCode());
    }
}
