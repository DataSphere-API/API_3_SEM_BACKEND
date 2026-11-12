package com.datasphere.fusex.guia;

import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.guiaProc.StatusProcedimento;

public enum StatusPainelBeneficiario {
    SOLICITADA,
    EMITIDA,
    REALIZADA,
    FATURADA,
    APROVADA,
    LIQUIDADA,
    EXPIRADA;

    public static StatusPainelBeneficiario doProcedimento(StatusProcedimento procedimento, StatusFatura fatura) {
        return switch (procedimento) {
            case PENDENTE, EM_ANDAMENTO -> EMITIDA;
            case REALIZADA -> REALIZADA;
            case EXPIRADA -> EXPIRADA;
            case FATURADA -> {
                if (fatura == StatusFatura.LIQUIDADA) yield LIQUIDADA;
                if (fatura == StatusFatura.APROVADA) yield APROVADA;
                yield FATURADA;
            }
        };
    }
}
