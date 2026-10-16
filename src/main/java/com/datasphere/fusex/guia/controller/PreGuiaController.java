package com.datasphere.fusex.guia.controller;

import com.datasphere.fusex.guia.dto.request.PreGuiaDetalheResponse;
import com.datasphere.fusex.guia.service.PreGuiaService;
import com.datasphere.fusex.guia.service.PreGuiaService.PreGuiaNaoEncontradaException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PreGuiaController {

    private final PreGuiaService preGuiaService;

    public PreGuiaController(PreGuiaService preGuiaService) {
        this.preGuiaService = preGuiaService;
    }

    @GetMapping("/pre-guias/{id}")
    public PreGuiaDetalheResponse detalhar(@PathVariable Long id) {
        return preGuiaService.buscarPreGuiaPorId(id);
    }

    @ExceptionHandler(PreGuiaNaoEncontradaException.class)
    public ResponseEntity<Map<String, String>> naoEncontrada(PreGuiaNaoEncontradaException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("codigo", "PRE_GUIA_NAO_ENCONTRADA", "mensagem", e.getMessage()));
    }
}