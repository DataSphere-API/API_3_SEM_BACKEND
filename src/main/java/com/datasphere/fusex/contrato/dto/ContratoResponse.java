package com.datasphere.fusex.contrato.dto;

import com.datasphere.fusex.contrato.ContratoModel;
import java.math.BigDecimal;

public record ContratoResponse(Integer id, String ocsCnpj, String ocsNome,
                               Integer procedimentoId, String procedimentoNome, BigDecimal valor) {
    public static ContratoResponse de(ContratoModel contrato) {
        return new ContratoResponse(contrato.getId(), contrato.getOcsModel().getCnpj(),
                contrato.getOcsModel().getNome(), contrato.getProcedimentoModel().getId(),
                contrato.getProcedimentoModel().getNome(), contrato.getValor());
    }
}
