package com.datasphere.fusex.procedimento.controller;

import com.datasphere.fusex.procedimento.dto.ProcedimentoRequest;
import com.datasphere.fusex.procedimento.dto.ProcedimentoResponse;
import com.datasphere.fusex.procedimento.dto.ProcedimentosResponse;
import com.datasphere.fusex.procedimento.service.CadastrarProcedimentoService;
import com.datasphere.fusex.procedimento.service.ListarProcedimentosService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
public class ProcedimentoController {

    private final CadastrarProcedimentoService cadastrarService;
    private final ListarProcedimentosService listarService;

    public ProcedimentoController(CadastrarProcedimentoService cadastrarService, ListarProcedimentosService listarService) {
        this.cadastrarService = cadastrarService;
        this.listarService = listarService;
    }

    @GetMapping("/procedimentos")
    public ResponseEntity<ProcedimentosResponse> listar() {
        return ResponseEntity.ok(listarService.listar());
    }

    @PostMapping("/procedimentos")
    public ResponseEntity<ProcedimentoResponse> cadastrar(@RequestBody ProcedimentoRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastrarService.cadastrar(request, principal));
    }
}
