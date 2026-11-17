package com.datasphere.fusex.preguia.controller;

import com.datasphere.fusex.dto.PreGuiaRequest;
import com.datasphere.fusex.dto.PreGuiaResponse;
import com.datasphere.fusex.preguia.service.PreGuiaService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pre-guias")
public class PreGuiaController {

    private final PreGuiaService preGuiaService;

    public PreGuiaController(PreGuiaService preGuiaService) {
        this.preGuiaService = preGuiaService;
    }

    @PostMapping
    public ResponseEntity<PreGuiaResponse> criar(@Valid @RequestBody PreGuiaRequest request) {
        Long emissorId = obterEmissorIdDoUsuarioAutenticado();
        PreGuiaResponse response = preGuiaService.criarPreGuia(request, emissorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PreGuiaResponse> editar(@PathVariable Long id,
                                                  @Valid @RequestBody PreGuiaRequest request) {
        Long emissorId = obterEmissorIdDoUsuarioAutenticado();
        PreGuiaResponse response = preGuiaService.editarPreGuia(id, request, emissorId);
        return ResponseEntity.ok(response);
    }

    private Long obterEmissorIdDoUsuarioAutenticado() {
        return 1L;
    }
}