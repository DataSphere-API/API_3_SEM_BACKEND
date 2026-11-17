package com.datasphere.fusex.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PreGuiaRequest {
    @NotBlank private String beneficiarioPrecCp;
    @NotNull private Long medicoId;
    @NotEmpty @Valid private List<ProcedimentoPreGuiaRequest> procedimentos;
}