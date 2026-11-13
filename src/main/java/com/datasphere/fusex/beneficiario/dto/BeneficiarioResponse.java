package com.datasphere.fusex.beneficiario.dto;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import java.time.LocalDate;

public record BeneficiarioResponse(String precCp, String nome, LocalDate dataNascimento,
                                   boolean dependente, String ug) {
    public static BeneficiarioResponse de(BeneficiarioModel beneficiario) {
        return new BeneficiarioResponse(beneficiario.getPrecCP(), beneficiario.getNome(),
                beneficiario.getDataNasc(), beneficiario.isDependente(), beneficiario.getUg());
    }
}
