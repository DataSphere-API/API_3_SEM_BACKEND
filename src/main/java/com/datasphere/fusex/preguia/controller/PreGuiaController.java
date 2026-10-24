package com.datasphere.fusex.preguia.controller;

import com.datasphere.fusex.preguia.dto.ValidarPreGuiaResponse;
import com.datasphere.fusex.preguia.service.ValidarPreGuiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PreGuiaController {

    private final ValidarPreGuiaService validarPreGuiaService;

    public PreGuiaController(ValidarPreGuiaService validarPreGuiaService) {
        this.validarPreGuiaService = validarPreGuiaService;
    }

    @PostMapping("/pre-guias/{id}/validar")
    public ResponseEntity<ValidarPreGuiaResponse> validar(@PathVariable Long id) {
        return ResponseEntity.ok(validarPreGuiaService.validarPreGuia(id));
    }
}