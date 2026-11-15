package com.datasphere.fusex.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ValidarQrCodeRequest(
        @NotBlank(message = "é obrigatório") String qr,
        @NotBlank(message = "é obrigatório") String cnpj
) {
}
