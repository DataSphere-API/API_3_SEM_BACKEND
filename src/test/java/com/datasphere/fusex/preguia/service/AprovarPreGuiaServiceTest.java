package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaRepository;
import com.datasphere.fusex.preguia.StatusPreGuia;
import com.datasphere.fusex.preguia.dto.AprovacaoResponse;
import com.datasphere.fusex.preguia.service.IdentificarAprovadorService.Aprovador;
import com.datasphere.fusex.preguia.service.IdentificarAprovadorService.Papel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    private PreGuiaRepository preGuiaRepository;
    @Mock
    private RegistrarStatusPreGuiaService registrarStatusPreGuiaService;

    @InjectMocks
    private AprovarPreGuiaService service;

    private PreGuiaModel preGuia(StatusPreGuia status) {
        PreGuiaModel p = new PreGuiaModel();
        p.setId(7L);
        p.setStatus(status);
        return p;
    }

    @Test
    void medicoAprova_registraADataEMantemValidada() {
        PreGuiaModel p = preGuia(StatusPreGuia.VALIDADA);
        when(buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(7L)).thenReturn(p);
        when(identificarAprovadorService.identificar("222", p)).thenReturn(new Aprovador(Papel.MEDICO, 1));

        AprovacaoResponse resposta = service.aprovar(7L, "222");

        assertNotNull(resposta.dataAprovacaoMedico());
        assertNull(resposta.dataAprovacaoChefe());
        assertEquals(StatusPreGuia.VALIDADA, resposta.status());
        verify(preGuiaRepository).save(p);
        verifyNoInteractions(registrarStatusPreGuiaService);
    }

    @Test
    void chefeAntesDoMedico_lancaAprovacaoMedicoPendente() {
        PreGuiaModel p = preGuia(StatusPreGuia.VALIDADA);
        when(buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(7L)).thenReturn(p);
        when(identificarAprovadorService.identificar("444", p)).thenReturn(new Aprovador(Papel.CHEFE, 1));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.aprovar(7L, "444"));

        assertEquals(ErrorCode.APROVACAO_MEDICO_PENDENTE, ex.getErrorCode());
        verifyNoInteractions(registrarStatusPreGuiaService);
    }

    @Test
    void chefeDepoisDoMedico_aprovaAPreGuia() {
        PreGuiaModel p = preGuia(StatusPreGuia.VALIDADA);
        p.setDataAprovacaoMedico(LocalDateTime.now());
        when(buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(7L)).thenReturn(p);
        when(identificarAprovadorService.identificar("444", p)).thenReturn(new Aprovador(Papel.CHEFE, 1));

        service.aprovar(7L, "444");

        assertEquals(1, p.getChefeId());
        assertNotNull(p.getDataAprovacaoChefe());
        verify(registrarStatusPreGuiaService).atualizarStatus(p, StatusPreGuia.APROVADA, "aprovacao-chefe");
    }

    @Test
    void medicoAprovandoDeNovo_lancaAprovacaoJaRegistrada() {
        PreGuiaModel p = preGuia(StatusPreGuia.VALIDADA);
        p.setDataAprovacaoMedico(LocalDateTime.now());
        when(buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(7L)).thenReturn(p);
        when(identificarAprovadorService.identificar("222", p)).thenReturn(new Aprovador(Papel.MEDICO, 1));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.aprovar(7L, "222"));

        assertEquals(ErrorCode.APROVACAO_JA_REGISTRADA, ex.getErrorCode());
    }

    @Test
    void preGuiaComPendencias_lancaPreGuiaNaoValidada() {
        PreGuiaModel p = preGuia(StatusPreGuia.COM_PENDENCIAS);
        when(buscarPreGuiaPorIdService.buscarPreGuiaParaAtualizar(7L)).thenReturn(p);
        when(identificarAprovadorService.identificar("222", p)).thenReturn(new Aprovador(Papel.MEDICO, 1));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.aprovar(7L, "222"));

        assertEquals(ErrorCode.PRE_GUIA_NAO_VALIDADA, ex.getErrorCode());
        verifyNoInteractions(registrarStatusPreGuiaService);
    }
}