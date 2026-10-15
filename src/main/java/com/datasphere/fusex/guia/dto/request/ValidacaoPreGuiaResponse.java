package com.datasphere.fusex.guia.dto.request;

import java.util.List;

public record ValidacaoPreGuiaResponse(boolean validada, List<PendenciaResponse> pendencias) {}