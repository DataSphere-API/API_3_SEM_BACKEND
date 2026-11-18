package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.StatusPreGuia;
import com.datasphere.fusex.preguia.dto.AprovacaoResponse;
import com.datasphere.fusex.preguia.service.IdentificarAprovadorService.Aprovador;
import com.datasphere.fusex.preguia.service.IdentificarAprovadorService.Papel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AprovarPreGuiaServiceTest {

    @Mock
    private BuscarPreGuiaPorIdService buscarPreGuiaPorIdService;
    @Mock
    private IdentificarAprovadorService identificarAprovadorService;
    @Mock
    private RegistrarStatusPreGuiaService registrarStatusPreGuiaService;

    private AprovarPreGuiaService service;

    private final Aprovador medico = new Aprovador(Papel.MEDICO, 1, 2, "Carlos Medeiros");
    private final Aprovador chefe = new Aprovador(Papel.CHEFE, 3, 1, "Roberto Lima");

    @BeforeEach
    void preparar() {
        service = new AprovarPreGuiaService(
                buscarPreGuiaPorIdService,
                identificarAprovadorService,
                new ValidarPreGuiaAguardandoDecisaoService(),
                new AssinarDecisaoDaPreGuiaService(),
                registrarStatusPreGuiaService,
                new MontarAssinaturaService());
    }

    private PreGuiaModel preGuia(StatusPreGuia status) {
        PreGuiaModel p = new PreGuiaModel();
        p.setId(7L);
        p.setStatus(status);
        p.setMedId(1);
        when(buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(7L)).thenReturn(p);
        return p;
    }

    @Test
    void medicoAprovaSozinho_preGuiaFicaAprovadaComAssinatura() {
        PreGuiaModel p = preGuia(StatusPreGuia.VALIDADA);
        when(identificarAprovadorService.identificar(p)).thenReturn(medico);

        AprovacaoResponse resposta = service.aprovar(7L);

        assertNotNull(resposta.dataAprovacaoMedico());
        assertNull(resposta.dataAprovacaoChefe());
        assertEquals(2, resposta.assinatura().usuarioId());
        assertEquals("Carlos Medeiros", resposta.assinatura().nome());
        assertEquals("MEDICO", resposta.assinatura().perfil());
        assertEquals(resposta.dataAprovacaoMedico(), resposta.assinatura().dataHora());
        verify(registrarStatusPreGuiaService).atualizarStatus(p, StatusPreGuia.APROVADA, "aprovacao-medico");
    }

    @Test
    void chefeAprovaSemOMedico_preGuiaFicaAprovadaComAssinatura() {
        PreGuiaModel p = preGuia(StatusPreGuia.VALIDADA);
        when(identificarAprovadorService.identificar(p)).thenReturn(chefe);

        AprovacaoResponse resposta = service.aprovar(7L);

        assertEquals(3, p.getChefeId());
        assertNotNull(p.getDataAprovacaoChefe());
        assertNull(p.getDataAprovacaoMedico());
        assertEquals("CHEFE", resposta.assinatura().perfil());
        assertEquals("Roberto Lima", p.getAssinaturaNome());
        verify(registrarStatusPreGuiaService).atualizarStatus(p, StatusPreGuia.APROVADA, "aprovacao-chefe");
    }

    @Test
    void preGuiaJaAprovada_lancaAprovacaoJaRegistrada() {
        PreGuiaModel p = preGuia(StatusPreGuia.APROVADA);
        p.setAssinaturaData(LocalDateTime.now());
        when(identificarAprovadorService.identificar(p)).thenReturn(chefe);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.aprovar(7L));

        assertEquals(ErrorCode.APROVACAO_JA_REGISTRADA, ex.getErrorCode());
        verifyNoInteractions(registrarStatusPreGuiaService);
    }

    @Test
    void preGuiaComPendencias_lancaPreGuiaNaoValidada() {
        PreGuiaModel p = preGuia(StatusPreGuia.COM_PENDENCIAS);
        when(identificarAprovadorService.identificar(p)).thenReturn(medico);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.aprovar(7L));

        assertEquals(ErrorCode.PRE_GUIA_NAO_VALIDADA, ex.getErrorCode());
        assertNull(p.getAssinaturaData());
        verifyNoInteractions(registrarStatusPreGuiaService);
    }
}
