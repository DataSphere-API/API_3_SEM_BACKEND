package com.datasphere.fusex.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "é obrigatório") String cpf,
        @NotBlank(message = "é obrigatória") String senha) {}
