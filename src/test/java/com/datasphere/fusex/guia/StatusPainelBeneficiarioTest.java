package com.datasphere.fusex.guia;

import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.guiaProc.StatusProcedimento;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusPainelBeneficiarioTest {

    @Test
    void mostraStatusDaFaturaApenasParaProcedimentoFaturado() {
        assertEquals(StatusPainelBeneficiario.FATURADA,
                StatusPainelBeneficiario.doProcedimento(StatusProcedimento.FATURADA, null));
        assertEquals(StatusPainelBeneficiario.APROVADA,
                StatusPainelBeneficiario.doProcedimento(StatusProcedimento.FATURADA, StatusFatura.APROVADA));
        assertEquals(StatusPainelBeneficiario.LIQUIDADA,
                StatusPainelBeneficiario.doProcedimento(StatusProcedimento.FATURADA, StatusFatura.LIQUIDADA));
        assertEquals(StatusPainelBeneficiario.EXPIRADA,
                StatusPainelBeneficiario.doProcedimento(StatusProcedimento.EXPIRADA, StatusFatura.LIQUIDADA));
        assertEquals(StatusPainelBeneficiario.EMITIDA,
                StatusPainelBeneficiario.doProcedimento(StatusProcedimento.EM_ANDAMENTO, null));
        assertEquals(StatusPainelBeneficiario.REALIZADA,
                StatusPainelBeneficiario.doProcedimento(StatusProcedimento.REALIZADA, null));
    }
}
