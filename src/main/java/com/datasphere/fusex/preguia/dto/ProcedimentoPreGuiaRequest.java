package com.datasphere.fusex.preguia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcedimentoPreGuiaRequest {

    @NotNull
    private Long procedimentoId;

    @NotBlank
    private String ocsCnpj;

    @NotNull
    private Integer quantidadeSessoes;
}