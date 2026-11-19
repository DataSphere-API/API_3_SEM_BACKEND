package com.datasphere.fusex.guia.dto;

import com.datasphere.fusex.guia.StatusPainelBeneficiario;

import java.time.LocalDate;

public record PreGuiaDoBeneficiarioResponse(Long preGuiaId, StatusPainelBeneficiario statusPainel,
                                            LocalDate dataCriacao) {
}
