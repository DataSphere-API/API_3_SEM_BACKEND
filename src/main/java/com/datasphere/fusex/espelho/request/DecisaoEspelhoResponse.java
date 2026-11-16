package com.datasphere.fusex.espelho.request;

import com.datasphere.fusex.espelho.StatusEspelho;

public record DecisaoEspelhoResponse(
        Integer id,
        StatusEspelho status,
        String motivo,
        Integer auditorId
) {
}
