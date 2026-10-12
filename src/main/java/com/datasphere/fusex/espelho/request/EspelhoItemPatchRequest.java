package com.datasphere.fusex.espelho.request;

import java.math.BigDecimal;

public record EspelhoItemPatchRequest(String beneficiarioId, String descricao, BigDecimal valor) {
}
