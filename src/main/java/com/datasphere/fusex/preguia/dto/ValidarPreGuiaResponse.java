package com.datasphere.fusex.preguia.dto;

import com.datasphere.fusex.preguia.StatusPreGuia;

import java.util.List;

public record ValidarPreGuiaResponse(
        Long preGuiaId,
        StatusPreGuia status,
        boolean validada,
        List<PendenciaResponse> pendencias) {}