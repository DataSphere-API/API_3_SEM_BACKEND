package com.datasphere.fusex.preguia.dto;

import com.datasphere.fusex.preguia.StatusPreGuia;

import java.time.LocalDateTime;

public record AprovacaoResponse(
        Long preGuiaId,
        StatusPreGuia status,
        Integer chefeId,
        LocalDateTime dataAprovacaoMedico,
        LocalDateTime dataAprovacaoChefe,
        String motivo) {}