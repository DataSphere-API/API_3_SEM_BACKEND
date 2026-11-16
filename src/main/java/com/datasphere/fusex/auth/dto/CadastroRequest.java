package com.datasphere.fusex.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CadastroRequest(
        @NotBlank(message = "é obrigatório") @Size(min = 3, max = 150, message = "deve ter entre 3 e 150 letras") String nome,
        @NotBlank(message = "é obrigatório") String precCp,
        @NotNull(message = "é obrigatória") @Past(message = "deve ser uma data passada") LocalDate dataNascimento,
        boolean dependente,
        @Pattern(regexp = "\\d{11}", message = "deve ter 11 dígitos") String cpfResponsavel,
        @NotBlank(message = "é obrigatório") @Pattern(regexp = "\\d{11}", message = "deve ter 11 dígitos") String cpf,
        @NotBlank(message = "é obrigatória") @Size(min = 8, message = "deve ter pelo menos 8 caracteres") String senha) {}
