package com.datasphere.fusex.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.ocs.OcsModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidacaoQrServiceTest {

    @Mock
    private GuiaProcRepository guiaProcRepository;

    @InjectMocks
    private ValidacaoQrService service;

    private GuiaProcModel procedimento(String cnpjOcs, String status) {
        OcsModel ocs = new OcsModel();
        ocs.setCnpj(cnpjOcs);

        ContratoModel contrato = new ContratoModel();
        contrato.setOcsModel(ocs);

        GuiaProcModel p = new GuiaProcModel();
        p.setContratoModel(contrato);
        p.setStatus(status);
        return p;
    }

    @Test
    void qrNaoEncontrado_lancaErro() {
        when(guiaProcRepository.findByQr("XYZ")).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.buscarProcedimentoPorQrCode("XYZ"));
        assertEquals(ErrorCode.QR_NAO_ENCONTRADO, ex.getErrorCode());
    }

    @Test
    void qrVazio_lancaErro() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.buscarProcedimentoPorQrCode("  "));
        assertEquals(ErrorCode.QR_NAO_ENCONTRADO, ex.getErrorCode());
    }

    @Test
    void qrEncontrado_retornaProcedimento() {
        GuiaProcModel p = procedimento("12345678000190", "PENDENTE");
        when(guiaProcRepository.findByQr("ABC")).thenReturn(p);

        assertSame(p, service.buscarProcedimentoPorQrCode("ABC"));
    }

    @Test
    void ocsDiferente_lancaErro() {
        GuiaProcModel p = procedimento("12345678000190", "PENDENTE");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.validarQrCodeDaOcs(p, "99999999000199"));
        assertEquals(ErrorCode.OCS_DIFERENTE_DO_CONTRATO, ex.getErrorCode());
    }

    @Test
    void ocsCorreta_comMascara_naoLancaErro() {
        GuiaProcModel p = procedimento("12345678000190", "PENDENTE");

        assertDoesNotThrow(() -> service.validarQrCodeDaOcs(p, "12.345.678/0001-90"));
    }

    @Test
    void procedimentoSemContrato_lancaErroDeOcs() {
        GuiaProcModel p = new GuiaProcModel();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.validarQrCodeDaOcs(p, "12345678000190"));
        assertEquals(ErrorCode.OCS_DIFERENTE_DO_CONTRATO, ex.getErrorCode());
    }

    @Test
    void procedimentoJaRealizado_lancaErro() {
        GuiaProcModel p = procedimento("12345678000190", "REALIZADO");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.validarProcedimentoNaoRealizado(p));
        assertEquals(ErrorCode.PROCEDIMENTO_JA_REALIZADO, ex.getErrorCode());
    }

    @Test
    void procedimentoPendente_naoLancaErro() {
        GuiaProcModel p = procedimento("12345678000190", "PENDENTE");

        assertDoesNotThrow(() -> service.validarProcedimentoNaoRealizado(p));
    }
}