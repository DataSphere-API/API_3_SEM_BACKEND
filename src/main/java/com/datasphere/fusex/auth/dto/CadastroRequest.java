package com.datasphere.fusex.auth.dto;

import java.time.LocalDate;

public record CadastroRequest(
        String nome,
        String precCp,
        LocalDate dataNascimento,
        boolean dependente,
        String cpfResponsavel,
        String cpf,
        String senha) {}