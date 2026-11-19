package com.datasphere.fusex.atendimento.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidarQrCodeRequest(
        @NotBlank(message = "é obrigatório") String qr
) {
}
