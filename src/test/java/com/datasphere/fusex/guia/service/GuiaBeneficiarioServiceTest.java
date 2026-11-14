package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuiaBeneficiarioServiceTest {

    @Mock private BeneficiarioRepository beneficiarioRepository;
    @Mock private GuiaRepository guiaRepository;
    @Mock private GuiaProcRepository procedimentoRepository;
    @Mock private ValidarConsultaGuiasService validarConsulta;

    private GuiaBeneficiarioService service() {
        return new GuiaBeneficiarioService(beneficiarioRepository, guiaRepository, procedimentoRepository,
                new ValidadeGuiaService(60), validarConsulta);
    }

    @Test
    void incluiNumeroNaRespostaPreservandoOsOutrosCampos() {
        Principal principal = () -> "login";
        var guia = new GuiaModel();
        guia.setId(1L);
        guia.setNumero("2026-000123");
        guia.setStatus("EMITIDA");
        guia.setDataEmissao(LocalDate.now());
        when(beneficiarioRepository.findByPrecCP("dono")).thenReturn(Optional.of(new BeneficiarioModel()));
        when(guiaRepository.findByBenfIdOrderByIdDesc("dono")).thenReturn(List.of(guia));
        when(procedimentoRepository.findByGuiaModel_Id(1L)).thenReturn(List.of());
        var resposta = service().listarGuiasPorBeneficiario("dono", principal).getFirst();
        assertEquals("2026-000123", resposta.numero());
        assertEquals(1L, resposta.guiaId());
        assertEquals(60, resposta.diasRestantes());
    }

    @Test
    void rejeitaAcessoAntesDeConsultarExistenciaDoBeneficiarioOuGuias() {
        Principal principal = () -> "login";
        doThrow(new BusinessException(ErrorCode.BENEFICIARIO_SEM_PERMISSAO))
                .when(validarConsulta).validarConsultaGuias("outro", principal);
        assertThrows(BusinessException.class, () -> service().listarGuiasPorBeneficiario("outro", principal));
        verifyNoInteractions(beneficiarioRepository, guiaRepository, procedimentoRepository);
    }
}
