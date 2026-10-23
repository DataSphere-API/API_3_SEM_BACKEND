package com.datasphere.fusex.guia.dto.request;

import java.util.List;

public record ValidarPreGuiaResponse(
        Long preGuiaId,
        String status,
        boolean validada,
        List<PendenciaResponse> pendencias) {}