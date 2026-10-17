package com.datasphere.fusex.guia.controller;

import com.datasphere.fusex.guia.dto.request.GuiaBeneficiarioResponse;
import com.datasphere.fusex.guia.service.GuiaBeneficiarioService;
import com.datasphere.fusex.guia.service.GuiaBeneficiarioService.BeneficiarioNaoEncontradoException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BeneficiarioGuiaController {

    private final GuiaBeneficiarioService guiaBeneficiarioService;

    public BeneficiarioGuiaController(GuiaBeneficiarioService guiaBeneficiarioService) {
        this.guiaBeneficiarioService = guiaBeneficiarioService;
    }

    @GetMapping("/beneficiarios/{precCp}/guias")
    public List<GuiaBeneficiarioResponse> listar(@PathVariable String precCp) {
        return guiaBeneficiarioService.listarGuiasPorBeneficiario(precCp);
    }

    @ExceptionHandler(BeneficiarioNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(BeneficiarioNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("codigo", "BENEFICIARIO_NAO_ENCONTRADO", "mensagem", e.getMessage()));
    }
}