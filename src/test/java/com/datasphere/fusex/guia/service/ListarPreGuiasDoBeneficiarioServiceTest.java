package com.datasphere.fusex.guia.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.StatusPainelBeneficiario;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaRepository;
import com.datasphere.fusex.preguia.StatusPreGuia;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ListarPreGuiasDoBeneficiarioServiceTest {

    private final PreGuiaRepository preGuiaRepository = mock(PreGuiaRepository.class);
    private final BuscarUsuarioLogadoService buscarUsuarioLogado = mock(BuscarUsuarioLogadoService.class);
    private final ListarPreGuiasDoBeneficiarioService service =
            new ListarPreGuiasDoBeneficiarioService(preGuiaRepository, buscarUsuarioLogado);

    private void logado(String precCp) {
        when(buscarUsuarioLogado.buscarUsuarioLogado()).thenReturn(new UsuarioLogado(
                9, "99351819019", "Fulano de Tal", Set.of(Perfil.BENEFICIARIO), precCp,
                null, null, null, null, null, null, null));
    }

    @Test
    void listaAsPreGuiasDoProprioBeneficiarioComoSolicitadasInclusiveAReprovada() {
        logado("123456789-00");
        PreGuiaModel preGuia = new PreGuiaModel();
        preGuia.setId(3L);
        when(preGuiaRepository.findByBenfIdAndGuiaIdIsNullAndStatusNotInOrderByIdDesc(
                "123456789-00", List.of(StatusPreGuia.EMITIDA)))
                .thenReturn(List.of(preGuia));

        var resposta = service.listar("123456789-00");

        assertEquals(1, resposta.size());
        assertEquals(3L, resposta.getFirst().preGuiaId());
        assertEquals(StatusPainelBeneficiario.SOLICITADA, resposta.getFirst().statusPainel());
    }

    @Test
    void naoMostraPreGuiaDeOutroBeneficiario() {
        logado("123456789-00");

        BusinessException erro = assertThrows(BusinessException.class, () -> service.listar("999999999-00"));
        assertEquals(ErrorCode.BENEFICIARIO_SEM_PERMISSAO, erro.getErrorCode());
        verifyNoInteractions(preGuiaRepository);
    }
}
