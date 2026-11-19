package com.datasphere.fusex.espelho.dto;

import com.datasphere.fusex.espelho.StatusEspelho;

public record DecisaoEspelhoResponse(
        Integer id,
        StatusEspelho status,
        String motivo,
        Integer auditorId
) {
}
