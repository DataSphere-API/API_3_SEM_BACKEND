package com.datasphere.fusex.guia;

import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.guiaproc.StatusProcedimento;

// Sequência que o beneficiário vê no painel: solicitada, emitida, realizada, faturada, aprovada, liquidada.
public enum StatusPainelBeneficiario {
    SOLICITADA,
    EMITIDA,
    REALIZADA,
    FATURADA,
    APROVADA,
    LIQUIDADA,
    EXPIRADA;

    public static StatusPainelBeneficiario doProcedimento(StatusProcedimento procedimento, StatusFatura fatura) {
        if (procedimento == StatusProcedimento.EXPIRADA) {
            return EXPIRADA;
        }
        if (procedimento == StatusProcedimento.REALIZADA) {
            return REALIZADA;
        }
        if (procedimento == StatusProcedimento.FATURADA) {
            if (fatura == StatusFatura.LIQUIDADA) {
                return LIQUIDADA;
            }
            if (fatura == StatusFatura.APROVADA) {
                return APROVADA;
            }
            return FATURADA;
        }
        return EMITIDA;
    }
}
