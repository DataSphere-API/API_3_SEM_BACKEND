package com.datasphere.fusex.guia.dto.request;

import java.time.LocalDateTime;

public record ExpiracaoResponse(int guiasExpiradas, int procedimentosExpirados, LocalDateTime executadoEm) {}