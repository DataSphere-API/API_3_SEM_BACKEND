package com.datasphere.fusex.preguia.controller;

import com.datasphere.fusex.preguia.dto.PreGuiaRequest;
import com.datasphere.fusex.preguia.dto.PreGuiaSalvaResponse;
import com.datasphere.fusex.preguia.service.CriarPreGuiaService;
import com.datasphere.fusex.preguia.service.EditarPreGuiaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class PreGuiaCadastroController {

    private final CriarPreGuiaService criarPreGuiaService;
    private final EditarPreGuiaService editarPreGuiaService;

    public PreGuiaCadastroController(CriarPreGuiaService criarPreGuiaService,
                                     EditarPreGuiaService editarPreGuiaService) {
        this.criarPreGuiaService = criarPreGuiaService;
        this.editarPreGuiaService = editarPreGuiaService;
    }

    @PostMapping("/pre-guias")
    public ResponseEntity<PreGuiaSalvaResponse> criar(@Valid @RequestBody PreGuiaRequest request) {
        var resposta = criarPreGuiaService.criar(request);
        return ResponseEntity.created(URI.create("/pre-guias/" + resposta.id())).body(resposta);
    }

    @PutMapping("/pre-guias/{id}")
    public ResponseEntity<PreGuiaSalvaResponse> editar(@PathVariable Long id,
                                                     @Valid @RequestBody PreGuiaRequest request) {
        return ResponseEntity.ok(editarPreGuiaService.editar(id, request));
    }
}
