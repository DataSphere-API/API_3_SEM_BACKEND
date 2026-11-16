package com.datasphere.fusex.service;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.dto.request.ConferenciaQrResponse;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import com.datasphere.fusex.guiaProc.GuiaProcRepository;
import com.datasphere.fusex.ocs.OcsModel;
import com.datasphere.fusex.procedimento.ProcedimentoModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConferenciaQrServiceTest {

    private static final String QR = "QR123";
    private static final String CNPJ = "12345678000199";

    @Mock
    private GuiaProcRepository guiaProcRepository;

    @Mock private com.datasphere.fusex.atendimento.service.BuscarOcsAutenticadaService ocsAutenticada;

    @InjectMocks
    private ValidacaoQrService service;

    private GuiaProcModel atendimento;

    @BeforeEach
    void setUp() {
        when(ocsAutenticada.buscarOcsAutenticada()).thenReturn(CNPJ);
        OcsModel ocs = new OcsModel();
        ocs.setCnpj(CNPJ);
        ocs.setNome("Clínica X");

        ProcedimentoModel procedimento = new ProcedimentoModel();
        procedimento.setId(1);
        procedimento.setNome("Sessão de psicoterapia");

        ContratoModel contrato = new ContratoModel();
        contrato.setOcsModel(ocs);
        contrato.setProcedimentoModel(procedimento);

        BeneficiarioModel beneficiario = new BeneficiarioModel();
        beneficiario.setPrecCP("123456");
        beneficiario.setNome("Fulano");

        GuiaModel guia = new GuiaModel();
        guia.setId(4L);
        guia.setBenfId("123456");
        // o campo da relação só tem getter; usamos reflexão simples via setter abaixo
        setBeneficiario(guia, beneficiario);

        atendimento = new GuiaProcModel();
        atendimento.setId(10L);
        atendimento.setContratoModel(contrato);
        atendimento.setGuiaModel(guia);
        atendimento.setQr(QR);
        atendimento.setStatus("EM_ANDAMENTO");
        atendimento.setQtdAutorizada(10);
        atendimento.setQtdRealizada(10);
        atendimento.setDataValidade(LocalDate.now().plusDays(30));
    }

    @Test
    void montaDadosCorretamenteSemAlterarNada() {
        when(guiaProcRepository.findByQr(QR)).thenReturn(atendimento);

        ConferenciaQrResponse r1 = service.montarDadosConferenciaQr(QR);
        ConferenciaQrResponse r2 = service.montarDadosConferenciaQr(QR);

        assertEquals(10L, r1.atendimentoId());
        assertEquals(4L, r1.guiaId());
        assertEquals("123456", r1.beneficiario().precCp());
        assertEquals("Fulano", r1.beneficiario().nome());
        assertEquals("Sessão de psicoterapia", r1.procedimento());
        assertEquals(CNPJ, r1.ocs().cnpj());
        assertEquals("Clínica X", r1.ocs().nome());
        assertEquals(10, r1.qtdAutorizada());
        assertEquals(10, r1.qtdRealizada());
        assertTrue(r1.prontoParaBaixa());
        assertEquals("EM_ANDAMENTO", r1.status());

        // várias chamadas, mesmo resultado
        assertEquals(r1, r2);

        // nada foi alterado na entidade
        assertEquals("EM_ANDAMENTO", atendimento.getStatus());
        assertEquals(10, atendimento.getQtdRealizada());

        // só houve leitura: nenhum save/delete no repository
        verify(guiaProcRepository, times(2)).findByQr(QR);
        verifyNoMoreInteractions(guiaProcRepository);
    }

    @Test
    void qrInexistente_lancaQrNaoEncontrado() {
        when(guiaProcRepository.findByQr("INEXISTENTE")).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.montarDadosConferenciaQr("INEXISTENTE"));

        assertEquals(ErrorCode.QR_NAO_ENCONTRADO, ex.getErrorCode());
    }

    @Test
    void ocsDiferente_lancaOcsDiferenteDoContrato() {
        when(ocsAutenticada.buscarOcsAutenticada()).thenReturn("99999999000100");
        when(guiaProcRepository.findByQr(QR)).thenReturn(atendimento);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.montarDadosConferenciaQr(QR));

        assertEquals(ErrorCode.OCS_DIFERENTE_DO_CONTRATO, ex.getErrorCode());
    }

    @Test
    void guiaVencida_lancaGuiaExpirada() {
        atendimento.setDataValidade(LocalDate.now().minusDays(1));
        when(guiaProcRepository.findByQr(QR)).thenReturn(atendimento);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.montarDadosConferenciaQr(QR));

        assertEquals(ErrorCode.GUIA_EXPIRADA, ex.getErrorCode());
    }

    private void setBeneficiario(GuiaModel guia, BeneficiarioModel beneficiario) {
        try {
            var campo = GuiaModel.class.getDeclaredField("beneficiarioModel");
            campo.setAccessible(true);
            campo.set(guia, beneficiario);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
