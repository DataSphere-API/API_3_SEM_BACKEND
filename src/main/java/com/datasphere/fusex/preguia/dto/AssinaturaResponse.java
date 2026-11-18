package com.datasphere.fusex.preguia.dto;

import java.time.LocalDateTime;

public record AssinaturaResponse(
        Integer usuarioId,
        String nome,
        String perfil,
        LocalDateTime dataHora) {}
