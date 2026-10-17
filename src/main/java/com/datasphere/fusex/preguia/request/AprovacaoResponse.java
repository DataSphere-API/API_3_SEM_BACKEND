package com.datasphere.fusex.preguia.request;

import com.datasphere.fusex.preguia.StatusPreGuia;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AprovacaoResponse(
        Long preGuiaId,
        StatusPreGuia status,
        Long chefeId,
        LocalDateTime dataAprovacao,
        String motivo
) {
}