package com.datasphere.fusex.guia.controller;

import com.datasphere.fusex.guia.dto.request.ValidarPreGuiaResponse;
import com.datasphere.fusex.guia.service.ValidarPreGuiaService;
import com.datasphere.fusex.guia.service.ValidarPreGuiaService.PreGuiaNaoEncontradaException;
import com.datasphere.fusex.guia.service.ValidarPreGuiaService.PreGuiaStatusInvalidoException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ValidarPreGuiaController {

    private final ValidarPreGuiaService validarPreGuiaService;

    public ValidarPreGuiaController(ValidarPreGuiaService validarPreGuiaService) {
        this.validarPreGuiaService = validarPreGuiaService;
    }

    @PostMapping("/pre-guias/{id}/validar")
    public ValidarPreGuiaResponse validar(@PathVariable Long id) {
        return validarPreGuiaService.validarPreGuia(id);
    }

    @ExceptionHandler(PreGuiaNaoEncontradaException.class)
    public ResponseEntity<Map<String, String>> naoEncontrada(PreGuiaNaoEncontradaException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("codigo", "PRE_GUIA_NAO_ENCONTRADA", "mensagem", e.getMessage()));
    }

    @ExceptionHandler(PreGuiaStatusInvalidoException.class)
    public ResponseEntity<Map<String, String>> statusInvalido(PreGuiaStatusInvalidoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("codigo", "PRE_GUIA_STATUS_INVALIDO", "mensagem", e.getMessage()));
    }
}