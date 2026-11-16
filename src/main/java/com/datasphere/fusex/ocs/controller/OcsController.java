package com.datasphere.fusex.ocs.controller;

import com.datasphere.fusex.ocs.dto.OcsRequest;
import com.datasphere.fusex.ocs.dto.OcsResponse;
import com.datasphere.fusex.ocs.dto.OcsListaResponse;
import com.datasphere.fusex.ocs.service.CadastrarOcsService;
import com.datasphere.fusex.ocs.service.ListarOcsService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
public class OcsController {

    private final CadastrarOcsService cadastrarService;
    private final ListarOcsService listarService;

    public OcsController(CadastrarOcsService cadastrarService, ListarOcsService listarService) {
        this.cadastrarService = cadastrarService;
        this.listarService = listarService;
    }

    @GetMapping("/ocs")
    public ResponseEntity<OcsListaResponse> listar() {
        return ResponseEntity.ok(listarService.listar());
    }

    @PostMapping("/ocs")
    public ResponseEntity<OcsResponse> cadastrar(@RequestBody OcsRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastrarService.cadastrar(request, principal));
    }
}
