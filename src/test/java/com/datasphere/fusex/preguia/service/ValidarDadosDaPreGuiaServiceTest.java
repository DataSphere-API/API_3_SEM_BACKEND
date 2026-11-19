package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import com.datasphere.fusex.contrato.ContratoRepository;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.medico.MedicoModel;
import com.datasphere.fusex.medico.MedicoRepository;
import com.datasphere.fusex.preguia.dto.PreGuiaRequest;
import com.datasphere.fusex.preguia.dto.ProcedimentoPreGuiaRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidarDadosDaPreGuiaServiceTest {

    @Mock private BeneficiarioRepository beneficiarioRepository;
    @Mock private MedicoRepository medicoRepository;
    @Mock private ContratoRepository contratoRepository;
    @InjectMocks private ValidarDadosDaPreGuiaService service;

    @Test
    void aceitaBeneficiarioMedicoEContratoValidos() {
        prepararBeneficiarioEMedico("SP123456");
        when(contratoRepository.existsByProcedimentoModel_IdAndOcsModel_Cnpj(1, "12345678000199"))
                .thenReturn(true);
        assertThatCode(() -> service.validar(pedido(item(1, "12345678000199"))))
                .doesNotThrowAnyException();
    }

    @Test
    void beneficiarioInexistenteNaoConsultaMedicoNemContrato() {
        assertThatThrownBy(() -> service.validar(pedido(item(1, "12345678000199"))))
                .isInstanceOf(BusinessException.class).extracting("errorCode")
                .isEqualTo(ErrorCode.BENEFICIARIO_NAO_ENCONTRADO);
        verifyNoInteractions(medicoRepository, contratoRepository);
    }

    @Test
    void medicoInexistenteDevolveErroDeDominio() {
        when(beneficiarioRepository.existsById("123456789-00")).thenReturn(true);
        assertThatThrownBy(() -> service.validar(pedido(item(1, "12345678000199"))))
                .isInstanceOf(BusinessException.class).extracting("errorCode")
                .isEqualTo(ErrorCode.MEDICO_NAO_ENCONTRADO);
        verifyNoInteractions(contratoRepository);
    }

    @Test
    void medicoSemCrmNaoPermiteCadastro() {
        prepararBeneficiarioEMedico("   ");
        assertThatThrownBy(() -> service.validar(pedido(item(1, "12345678000199"))))
                .isInstanceOf(BusinessException.class).extracting("errorCode")
                .isEqualTo(ErrorCode.MEDICO_SEM_CRM);
        verifyNoInteractions(contratoRepository);
    }

    @Test
    void mesmoProcedimentoNaMesmaOcsDevolveProcedimentoRepetido() {
        prepararBeneficiarioEMedico("SP123456");
        when(contratoRepository.existsByProcedimentoModel_IdAndOcsModel_Cnpj(1, "12345678000199"))
                .thenReturn(true);
        var item = item(1, "12345678000199");
        assertThatThrownBy(() -> service.validar(pedido(item, item)))
                .isInstanceOf(BusinessException.class).extracting("errorCode")
                .isEqualTo(ErrorCode.PROCEDIMENTO_REPETIDO);
    }

    @Test
    void mesmoProcedimentoEmOcsDiferentesEPermitido() {
        prepararBeneficiarioEMedico("SP123456");
        when(contratoRepository.existsByProcedimentoModel_IdAndOcsModel_Cnpj(1, "12345678000199"))
                .thenReturn(true);
        when(contratoRepository.existsByProcedimentoModel_IdAndOcsModel_Cnpj(1, "98765432000111"))
                .thenReturn(true);
        assertThatCode(() -> service.validar(pedido(item(1, "12345678000199"), item(1, "98765432000111"))))
                .doesNotThrowAnyException();
    }

    @Test
    void procedimentoSemContratoERecusado() {
        prepararBeneficiarioEMedico("SP123456");
        assertThatThrownBy(() -> service.validar(pedido(item(6, "12345678000199"))))
                .isInstanceOf(BusinessException.class).extracting("errorCode")
                .isEqualTo(ErrorCode.PROCEDIMENTO_SEM_CONTRATO);
    }

    private void prepararBeneficiarioEMedico(String crm) {
        when(beneficiarioRepository.existsById("123456789-00")).thenReturn(true);
        var medico = new MedicoModel();
        medico.setId(1);
        medico.setCrm(crm);
        when(medicoRepository.findById(1)).thenReturn(Optional.of(medico));
    }

    private PreGuiaRequest pedido(ProcedimentoPreGuiaRequest... itens) {
        return new PreGuiaRequest("123456789-00", 1, List.of(itens));
    }

    private ProcedimentoPreGuiaRequest item(int id, String cnpj) {
        return new ProcedimentoPreGuiaRequest(id, cnpj, 2);
    }
}
