package com.datasphere.fusex.contrato.controller;

import com.datasphere.fusex.contrato.dto.ContratoRequest;
import com.datasphere.fusex.contrato.dto.ContratoResponse;
import com.datasphere.fusex.contrato.dto.ContratosResponse;
import com.datasphere.fusex.contrato.service.CadastrarContratoService;
import com.datasphere.fusex.contrato.service.ListarContratosService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
public class ContratoController {

    private final CadastrarContratoService cadastrarService;
    private final ListarContratosService listarService;

    public ContratoController(CadastrarContratoService cadastrarService, ListarContratosService listarService) {
        this.cadastrarService = cadastrarService;
        this.listarService = listarService;
    }

    @GetMapping("/contratos")
    public ResponseEntity<ContratosResponse> listar() {
        return ResponseEntity.ok(listarService.listar());
    }

    @PostMapping("/contratos")
    public ResponseEntity<ContratoResponse> cadastrar(@RequestBody ContratoRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastrarService.cadastrar(request, principal));
    }
}
