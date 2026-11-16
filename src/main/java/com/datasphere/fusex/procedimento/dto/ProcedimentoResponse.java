package com.datasphere.fusex.procedimento.dto;

import com.datasphere.fusex.procedimento.ProcedimentoModel;

public record ProcedimentoResponse(Integer id, String nome) {
    public static ProcedimentoResponse de(ProcedimentoModel procedimento) {
        return new ProcedimentoResponse(procedimento.getId(), procedimento.getNome());
    }
}
