package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.auth.service.BuscarUsuarioLogadoService;
import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.guia.GuiaModel;
import com.datasphere.fusex.guia.GuiaRepository;
import com.datasphere.fusex.guia.service.ValidadeGuiaService;
import com.datasphere.fusex.preguia.PreGuiaModel;
import com.datasphere.fusex.preguia.PreGuiaProcRepository;
import com.datasphere.fusex.preguia.PreGuiaRepository;
import com.datasphere.fusex.preguia.StatusPreGuia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListarPreGuiasServiceTest {

    @Mock private PreGuiaRepository preGuiaRepository;
    @Mock private PreGuiaProcRepository preGuiaProcRepository;
    @Mock private BuscarUsuarioLogadoService buscarUsuarioLogadoService;
    @Mock private CalcularValorTotalPreGuiaService calcularValorTotalPreGuiaService;
    @Mock private BuscarDataEmissaoDaPreGuiaService buscarDataEmissaoDaPreGuiaService;
    @Mock private GuiaRepository guiaRepository;
    @Mock private ValidadeGuiaService validadeGuiaService;
    @InjectMocks private ListarPreGuiasService service;

    private PreGuiaModel preGuia(long id, Long guiaId) {
        BeneficiarioModel beneficiario = new BeneficiarioModel();
        beneficiario.setNome("Ricardo");
        PreGuiaModel preGuia = new PreGuiaModel();
        preGuia.setId(id);
        preGuia.setBenfId("123456789-00");
        preGuia.setBeneficiarioModel(beneficiario);
        preGuia.setGuiaId(guiaId);
        preGuia.setStatus(guiaId == null ? StatusPreGuia.VALIDADA : StatusPreGuia.EMITIDA);
        return preGuia;
    }

    @Test
    void listaComPrecCpNumeroDaGuiaEValidade() {
        when(buscarUsuarioLogadoService.buscarUsuarioLogado()).thenReturn(new UsuarioLogado(1, "1", "Emissor",
                Set.of(Perfil.EMISSOR), null, null, null, null, 1, null, null, null));
        PreGuiaModel emitida = preGuia(5L, 9L);
        PreGuiaModel emAndamento = preGuia(6L, null);
        when(preGuiaRepository.findAllByOrderByIdAsc()).thenReturn(List.of(emitida, emAndamento));
        when(buscarDataEmissaoDaPreGuiaService.buscarDataEmissao(emitida)).thenReturn(LocalDate.of(2026, 11, 1));
        when(buscarDataEmissaoDaPreGuiaService.buscarDataEmissao(emAndamento)).thenReturn(null);
        when(validadeGuiaService.calcularDataValidade(LocalDate.of(2026, 11, 1))).thenReturn(LocalDate.of(2026, 12, 31));
        GuiaModel guia = mock(GuiaModel.class);
        when(guia.getNumero()).thenReturn("2026-000009");
        when(guiaRepository.findById(9L)).thenReturn(Optional.of(guia));

        var lista = service.listarPreGuiasPorStatus(null, false);

        assertThat(lista.get(0).beneficiarioPrecCp()).isEqualTo("123456789-00");
        assertThat(lista.get(0).guiaNumero()).isEqualTo("2026-000009");
        assertThat(lista.get(0).validoAte()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(lista.get(1).guiaNumero()).isNull();
        assertThat(lista.get(1).validoAte()).isNull();
    }
}
