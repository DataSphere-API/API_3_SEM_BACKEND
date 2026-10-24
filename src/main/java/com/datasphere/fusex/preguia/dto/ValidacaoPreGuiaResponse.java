package com.datasphere.fusex.preguia.dto;

import java.util.List;

public record ValidacaoPreGuiaResponse(boolean validada, List<PendenciaResponse> pendencias) {}