package com.datasphere.fusex.lisura.dto;

import java.util.List;

public record LisuraResponse(
        Long faturaId,
        List<LisuraItemResponse> itens
) {}