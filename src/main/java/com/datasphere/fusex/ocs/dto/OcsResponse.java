package com.datasphere.fusex.ocs.dto;

import com.datasphere.fusex.ocs.OcsModel;

public record OcsResponse(String cnpj, String nome) {
    public static OcsResponse de(OcsModel ocs) {
        return new OcsResponse(ocs.getCnpj(), ocs.getNome());
    }
}
