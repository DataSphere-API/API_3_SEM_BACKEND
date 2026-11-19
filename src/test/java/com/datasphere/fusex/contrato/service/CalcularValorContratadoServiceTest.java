package com.datasphere.fusex.contrato.service;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CalcularValorContratadoServiceTest {

    private final CalcularValorContratadoService service = new CalcularValorContratadoService();

    private GuiaProcModel atendimento(String valorDoContrato, int sessoes) {
        ContratoModel contrato = new ContratoModel();
        contrato.setValor(valorDoContrato == null ? null : new BigDecimal(valorDoContrato));
        GuiaProcModel atendimento = new GuiaProcModel();
        atendimento.setContratoModel(contrato);
        atendimento.setQtdAutorizada(sessoes);
        return atendimento;
    }

    @Test
    void multiplicaOValorDoContratoPelasSessoesPrevistas() {
        assertThat(service.calcular(atendimento("80.00", 10))).isEqualByComparingTo("800.00");
    }

    @Test
    void atendimentoDeUmaSessaoUsaOValorDoContrato() {
        assertThat(service.calcular(atendimento("150.00", 1))).isEqualByComparingTo("150.00");
    }

    @Test
    void semContratoDevolveNull() {
        GuiaProcModel semContrato = new GuiaProcModel();
        semContrato.setQtdAutorizada(3);
        assertThat(service.calcular(semContrato)).isNull();
        assertThat(service.calcular(null)).isNull();
    }
}
