package com.datasphere.fusex.guia.controller;

import com.datasphere.fusex.guia.dto.request.ExpiracaoResponse;
import com.datasphere.fusex.guia.service.ExpirarGuiasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExpiracaoController {

    private final ExpirarGuiasService expirarGuiasService;

    public ExpiracaoController(ExpirarGuiasService expirarGuiasService) {
        this.expirarGuiasService = expirarGuiasService;
    }

    @PostMapping("/admin/guias/expirar")
    public ResponseEntity<ExpiracaoResponse> expirar() {
        return ResponseEntity.ok(expirarGuiasService.expirarGuiasVencidas());
    }
}