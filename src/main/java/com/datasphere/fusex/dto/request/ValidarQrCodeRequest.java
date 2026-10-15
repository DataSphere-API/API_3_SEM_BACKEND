package com.datasphere.fusex.dto.request;

public record ValidarQrCodeRequest(
        String qr,
        String cnpj
) {
}