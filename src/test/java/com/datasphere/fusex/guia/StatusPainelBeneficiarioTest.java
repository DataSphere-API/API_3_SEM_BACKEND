package com.datasphere.fusex.guia;

import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.guiaproc.StatusProcedimento;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusPainelBeneficiarioTest {

    @Test
    void procedimentoSemFaturaSegueOStatusDoAtendimento() {
        assertEquals(StatusPainelBeneficiario.EMITIDA,
                StatusPainelBeneficiario.doProcedimento(StatusProcedimento.PENDENTE, null));
        assertEquals(StatusPainelBeneficiario.EMITIDA,
                StatusPainelBeneficiario.doProcedimento(StatusProcedimento.EM_ANDAMENTO, null));
        assertEquals(StatusPainelBeneficiario.REALIZADA,
                StatusPainelBeneficiario.doProcedimento(StatusProcedimento.REALIZADA, null));
        assertEquals(StatusPainelBeneficiario.EXPIRADA,
                StatusPainelBeneficiario.doProcedimento(StatusProcedimento.EXPIRADA, null));
    }

    @Test
    void procedimentoFaturadoMostraOAndamentoDaFatura() {
        assertEquals(StatusPainelBeneficiario.FATURADA,
                StatusPainelBeneficiario.doProcedimento(StatusProcedimento.FATURADA, StatusFatura.ATIVA));
        assertEquals(StatusPainelBeneficiario.APROVADA,
                StatusPainelBeneficiario.doProcedimento(StatusProcedimento.FATURADA, StatusFatura.APROVADA));
        assertEquals(StatusPainelBeneficiario.LIQUIDADA,
                StatusPainelBeneficiario.doProcedimento(StatusProcedimento.FATURADA, StatusFatura.LIQUIDADA));
    }
}
