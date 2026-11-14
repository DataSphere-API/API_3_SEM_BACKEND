package com.datasphere.fusex.guia.controller;

import com.datasphere.fusex.guia.dto.request.GuiaBeneficiarioResponse;
import com.datasphere.fusex.guia.service.GuiaBeneficiarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.security.Principal;

@RestController
public class BeneficiarioGuiaController {

    private final GuiaBeneficiarioService guiaBeneficiarioService;

    public BeneficiarioGuiaController(GuiaBeneficiarioService guiaBeneficiarioService) {
        this.guiaBeneficiarioService = guiaBeneficiarioService;
    }

    @GetMapping("/beneficiarios/{precCp}/guias")
    public ResponseEntity<List<GuiaBeneficiarioResponse>> listar(@PathVariable String precCp, Principal principal) {
        return ResponseEntity.ok(guiaBeneficiarioService.listarGuiasPorBeneficiario(precCp, principal));
    }
}
